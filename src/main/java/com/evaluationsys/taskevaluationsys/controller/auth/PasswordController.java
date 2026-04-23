package com.evaluationsys.taskevaluationsys.controller.auth;

import com.evaluationsys.taskevaluationsys.security.CustomUserDetails;
import com.evaluationsys.taskevaluationsys.service.auth.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class PasswordController {

    @Autowired
    private PasswordService passwordService;

    // =========================
    // FORGOT PASSWORD PAGES (Public) - /auth/**
    // =========================
    @GetMapping("/auth/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @GetMapping("/auth/verify-otp")
    public String verifyOtpPage(@RequestParam(required = false) String email, Model model) {
        System.out.println("=== VERIFY OTP PAGE ===");
        System.out.println("Email from URL parameter: '" + email + "'");
        model.addAttribute("email", email);
        return "auth/verify-otp";
    }

    @GetMapping("/auth/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    // =========================
    // API: PASSWORD RESET (Public)
    // =========================
    @PostMapping("/auth/password/forgot")
    @ResponseBody
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String staffCodeOrEmail = request.get("staffCodeOrEmail");
        System.out.println("=== FORGOT PASSWORD API ===");
        System.out.println("Request for: " + staffCodeOrEmail);

        if (staffCodeOrEmail == null || staffCodeOrEmail.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Staff code or email is required"));
        }

        try {
            PasswordService.PasswordResetResponse response = passwordService.requestPasswordReset(staffCodeOrEmail);
            System.out.println("Response email: " + response.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/auth/password/verify-otp")
    @ResponseBody
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        System.out.println("=== VERIFY OTP API ===");
        System.out.println("Email received: '" + email + "'");

        if (email == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and OTP are required"));
        }

        PasswordService.OtpVerificationResponse response = passwordService.verifyOtp(email, otp);
        System.out.println("Verification success: " + response.isSuccess());

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/auth/password/reset")
    @ResponseBody
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String resetToken = request.get("resetToken");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");

        if (resetToken == null || newPassword == null || confirmPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));
        }

        PasswordService.PasswordResetResponse response = passwordService.resetPassword(resetToken, newPassword, confirmPassword);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/auth/password/resend-otp")
    @ResponseBody
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        System.out.println("=== RESEND OTP API ===");
        System.out.println("Email: " + email);

        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        try {
            PasswordService.PasswordResetResponse response = passwordService.resendOtp(email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================
    // CHANGE PASSWORD (Authenticated Users) - /profile/**
    // =========================
    @GetMapping("/profile/change-password")
    public String changePasswordPage(Model model) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("currentUser", userDetails.getUser());
        return "profile/change-password";
    }

    @PostMapping("/profile/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "User not authenticated"
            ));
        }

        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");

        if (currentPassword == null || newPassword == null || confirmPassword == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "All fields are required"
            ));
        }

        try {
            PasswordService.ChangePasswordResponse response = passwordService.changePassword(
                    userDetails.getUser(), currentPassword, newPassword, confirmPassword
            );

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // =========================
    // HELPER
    // =========================
    private CustomUserDetails getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }
        return null;
    }
}