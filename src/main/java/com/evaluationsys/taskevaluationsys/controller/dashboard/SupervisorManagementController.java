package com.evaluationsys.taskevaluationsys.controller.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard/supervisors")
public class SupervisorManagementController {

    @GetMapping
    public String supervisorPage() {
        return "dashboard/supervisor";
    }
}