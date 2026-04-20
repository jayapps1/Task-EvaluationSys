package com.evaluationsys.taskevaluationsys.service.auth;

import com.evaluationsys.taskevaluationsys.dto.auth.LoginRequestDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.auth.LoginDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // assume you have a JWT utility

    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginDTOResponse login(LoginRequestDTO request) {
        User user = userRepository.findByStaffCode(request.getStaffCode())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user);

        // Update last login
        user.setLastLogIn(java.time.LocalDateTime.now());
        userRepository.save(user);

        return new LoginDTOResponse(token, user.getRole().name(), user.getFirstName());
    }
}