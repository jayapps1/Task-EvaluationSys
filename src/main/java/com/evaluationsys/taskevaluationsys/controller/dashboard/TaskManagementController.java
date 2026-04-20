package com.evaluationsys.taskevaluationsys.controller.dashboard;

import com.evaluationsys.taskevaluationsys.repository.SupervisorRepository;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TaskManagementController {

    private final SupervisorRepository supervisorRepository;
    private final UserRepository userRepository;

    public TaskManagementController(SupervisorRepository supervisorRepository,
                                    UserRepository userRepository) {
        this.supervisorRepository = supervisorRepository;
        this.userRepository = userRepository;
    }

    // ===============================
    // TASK DASHBOARD
    // ===============================
    @GetMapping("/dashboard/tasks")
    public String taskDashboard(Model model) {
        // Fetch all supervisors for "Assigned Supervisor" dropdown
        model.addAttribute("supervisors", supervisorRepository.findAll());

        // Fetch all users for "Created By" dropdown
        model.addAttribute("users", userRepository.findAll());

        // Return Thymeleaf template path
        return "dashboard/task"; // ensure this matches your template
    }
}