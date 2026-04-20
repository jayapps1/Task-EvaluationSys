package com.evaluationsys.taskevaluationsys.controller.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserManagementController {

    @GetMapping("/dashboard/users")
    public String usersPage(Model model) {
        // You can pass data for your users table if needed
        return "dashboard/users"; // Thymeleaf template: users.html
    }
}