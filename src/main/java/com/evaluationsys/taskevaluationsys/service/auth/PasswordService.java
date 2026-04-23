package com.evaluationsys.taskevaluationsys.service.auth;

import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    // Store OTP temporarily (in production, use Redis or database)
    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    // Store reset tokens
    private final Map<String, ResetTokenData> resetTokenStore = new ConcurrentHashMap<>();

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 15;
    private static final SecureRandom random = new SecureRandom();

    // =========================
    // STEP 1: REQUEST PASSWORD RESET (SEND OTP)
    // =========================
    public PasswordResetResponse requestPasswordReset(String staffCodeOrEmail) {
        User user = userRepository.findByStaffCodeOrEmail(staffCodeOrEmail)
                .orElseThrow(() -> new RuntimeException("User not found with provided staff code or email"));

        // Generate 6-digit OTP
        String otp = generateOtp();

        // ✅ Store OTP with the REAL email
        String realEmail = user.getEmail();
        otpStore.put(realEmail, new OtpData(otp, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));

        System.out.println("OTP stored for: " + realEmail + " -> " + otp);

        // Send OTP via email
        sendOtpEmail(realEmail, otp, user.getFirstName());

        return new PasswordResetResponse(
                true,
                "OTP sent to your registered email address",
                realEmail,              // ✅ Real email for API
                maskEmail(realEmail)    // ✅ Masked for display
        );
    }

    // =========================
    // STEP 2: VERIFY OTP
    // =========================
    public OtpVerificationResponse verifyOtp(String email, String otp) {
        System.out.println("Verifying OTP for: " + email);
        System.out.println("OTP received: " + otp);
        System.out.println("OTP store keys: " + otpStore.keySet());

        OtpData storedOtp = otpStore.get(email);

        if (storedOtp == null) {
            return new OtpVerificationResponse(false, "No OTP request found. Please request a new OTP.", null);
        }

        if (storedOtp.isExpired()) {
            otpStore.remove(email);
            return new OtpVerificationResponse(false, "OTP has expired. Please request a new OTP.", null);
        }

        if (!storedOtp.getOtp().equals(otp)) {
            return new OtpVerificationResponse(false, "Invalid OTP. Please try again.", null);
        }

        // OTP verified - generate reset token
        String resetToken = generateResetToken();
        resetTokenStore.put(resetToken, new ResetTokenData(email, LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES)));

        // Remove OTP after successful verification
        otpStore.remove(email);

        return new OtpVerificationResponse(true, "OTP verified successfully", resetToken);
    }

    // =========================
    // STEP 3: RESET PASSWORD WITH TOKEN
    // =========================
    public PasswordResetResponse resetPassword(String resetToken, String newPassword, String confirmPassword) {
        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            return new PasswordResetResponse(false, "Passwords do not match", null, null);
        }

        // Validate password strength
        if (!isPasswordStrong(newPassword)) {
            return new PasswordResetResponse(false,
                    "Password must be at least 8 characters with uppercase, lowercase, number, and special character",
                    null, null);
        }

        ResetTokenData tokenData = resetTokenStore.get(resetToken);

        if (tokenData == null) {
            return new PasswordResetResponse(false, "Invalid or expired reset token. Please request a new password reset.", null, null);
        }

        if (tokenData.isExpired()) {
            resetTokenStore.remove(resetToken);
            return new PasswordResetResponse(false, "Reset token has expired. Please request a new password reset.", null, null);
        }

        // Find user by email
        User user = userRepository.findByEmail(tokenData.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Remove used token
        resetTokenStore.remove(resetToken);

        // Send confirmation email
        sendPasswordChangedEmail(user.getEmail(), user.getFirstName());

        return new PasswordResetResponse(true, "Password reset successfully. You can now log in with your new password.", null, null);
    }

    // =========================
    // RESEND OTP
    // =========================
    public PasswordResetResponse resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        // Remove old OTP if exists
        otpStore.remove(email);

        // Generate new OTP
        String otp = generateOtp();
        otpStore.put(email, new OtpData(otp, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));

        System.out.println("OTP resent for: " + email + " -> " + otp);

        // Send OTP via email
        sendOtpEmail(email, otp, user.getFirstName());

        return new PasswordResetResponse(
                true,
                "New OTP sent to your email",
                email,              // ✅ Real email
                maskEmail(email)    // ✅ Masked for display
        );
    }

    // =========================
    // CHANGE PASSWORD (Authenticated User)
    // =========================
    public ChangePasswordResponse changePassword(User user, String currentPassword,
                                                 String newPassword, String confirmPassword) {
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            return new ChangePasswordResponse(false, "Current password is incorrect");
        }

        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            return new ChangePasswordResponse(false, "New passwords do not match");
        }

        // Validate password strength
        if (!isPasswordStrong(newPassword)) {
            return new ChangePasswordResponse(false,
                    "Password must be at least 8 characters with uppercase, lowercase, number, and special character");
        }

        // Check if new password is different from current
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            return new ChangePasswordResponse(false, "New password must be different from current password");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Send confirmation email
        sendPasswordChangedEmail(user.getEmail(), user.getFirstName());

        return new ChangePasswordResponse(true, "Password changed successfully");
    }

    // =========================
    // HELPER METHODS
    // =========================
    private String generateOtp() {
        return String.format("%06d", random.nextInt(1000000));
    }

    private String generateResetToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email;
        return email.substring(0, 3) + "***" + email.substring(atIndex);
    }

    private void sendOtpEmail(String to, String otp, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset OTP - Task Evaluation System");
        message.setText(String.format("""
                Dear %s,
                
                You have requested to reset your password.
                
                Your OTP is: %s
                
                This OTP will expire in %d minutes.
                
                If you did not request this, please ignore this email.
                
                Best regards,
                Task Evaluation System Team
                """, firstName != null ? firstName : "User", otp, OTP_EXPIRY_MINUTES));

        mailSender.send(message);
    }

    private void sendPasswordChangedEmail(String to, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Changed Successfully - Task Evaluation System");
        message.setText(String.format("""
                Dear %s,
                
                Your password has been successfully changed.
                
                If you did not make this change, please contact support immediately.
                
                Best regards,
                Task Evaluation System Team
                """, firstName != null ? firstName : "User"));

        mailSender.send(message);
    }

    // =========================
    // INNER CLASSES
    // =========================
    private static class OtpData {
        private final String otp;
        private final LocalDateTime expiryTime;

        public OtpData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        public String getOtp() { return otp; }
        public boolean isExpired() { return LocalDateTime.now().isAfter(expiryTime); }

        public LocalDateTime getExpiryTime() { return expiryTime; }
    }

    private static class ResetTokenData {
        private final String email;
        private final LocalDateTime expiryTime;

        public ResetTokenData(String email, LocalDateTime expiryTime) {
            this.email = email;
            this.expiryTime = expiryTime;
        }

        public String getEmail() { return email; }
        public boolean isExpired() { return LocalDateTime.now().isAfter(expiryTime); }
    }

    // =========================
    // RESPONSE DTOs
    // =========================
    public static class PasswordResetResponse {
        private final boolean success;
        private final String message;
        private final String email;        // ✅ Real email for API
        private final String maskedEmail;  // ✅ Masked for display

        public PasswordResetResponse(boolean success, String message, String email, String maskedEmail) {
            this.success = success;
            this.message = message;
            this.email = email;
            this.maskedEmail = maskedEmail;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getEmail() { return email; }
        public String getMaskedEmail() { return maskedEmail; }
    }

    public static class OtpVerificationResponse {
        private final boolean success;
        private final String message;
        private final String resetToken;

        public OtpVerificationResponse(boolean success, String message, String resetToken) {
            this.success = success;
            this.message = message;
            this.resetToken = resetToken;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getResetToken() { return resetToken; }
    }

    public static class ChangePasswordResponse {
        private final boolean success;
        private final String message;

        public ChangePasswordResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}