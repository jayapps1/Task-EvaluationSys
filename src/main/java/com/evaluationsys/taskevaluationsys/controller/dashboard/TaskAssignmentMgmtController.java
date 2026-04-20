package com.evaluationsys.taskevaluationsys.controller.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping()
public class TaskAssignmentMgmtController {

    @GetMapping("/dashboard/taskassignments")
    public String dashboardPage() {
        return "dashboard/taskassignment"; // Thymeleaf template path
    }
}