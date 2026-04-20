package com.evaluationsys.taskevaluationsys.controller.dashboard;

import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoleAssignController {

    @GetMapping("/role-management")
    public String roleManagementPage(Model model) {
        // Get the currently logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Cast to your CustomUserDetails
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        // Get the actual User entity
        User currentUser = userDetails.getUser();

        // Add staff code to model for Thymeleaf
        model.addAttribute("loggedInAdminStaffCode", currentUser.getStaffCode());

        return "dashboard/assign_role"; // Thymeleaf template path
    }
}