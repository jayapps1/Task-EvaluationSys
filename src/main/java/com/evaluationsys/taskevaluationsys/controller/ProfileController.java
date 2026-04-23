package com.evaluationsys.taskevaluationsys.controller;

import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
import com.evaluationsys.taskevaluationsys.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    // =========================
    // VIEW PROFILE PAGE
    // =========================
    @GetMapping
    public String viewProfile(Model model) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        User user = userDetails.getUser();
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "My Profile");

        return "profile/index";
    }

    // =========================
    // EDIT PROFILE PAGE
    // =========================
    @GetMapping("/edit")
    public String editProfilePage(Model model) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        User user = userDetails.getUser();
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Edit Profile");

        return "profile/edit";
    }

    // =========================
    // API: UPDATE PROFILE
    // =========================
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> request) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "User not authenticated"
            ));
        }

        User user = userDetails.getUser();

        String firstName = request.get("firstName");
        String otherName = request.get("otherName");
        String email = request.get("email");
        String phoneNumber = request.get("phoneNumber");

        // Validate required fields
        if (firstName == null || firstName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "First name is required"
            ));
        }

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email is required"
            ));
        }

        // Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid email format"
            ));
        }

        // Check if email is already taken by another user
        userRepository.findByEmail(email).ifPresent(existingUser -> {
            if (!existingUser.getStaffId().equals(user.getStaffId())) {
                throw new RuntimeException("Email is already taken by another user");
            }
        });

        try {
            // Update user information
            user.setFirstName(firstName);
            user.setOtherName(otherName != null ? otherName : "");
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber != null ? phoneNumber : "");

            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("user", Map.of(
                    "firstName", user.getFirstName(),
                    "otherName", user.getOtherName(),
                    "email", user.getEmail(),
                    "phoneNumber", user.getPhoneNumber()
            ));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to update profile"
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