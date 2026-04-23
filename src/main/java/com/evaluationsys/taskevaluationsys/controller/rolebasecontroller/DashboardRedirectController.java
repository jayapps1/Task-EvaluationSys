package com.evaluationsys.taskevaluationsys.controller.rolebasecontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardRedirectController {

    @GetMapping("/dashboard/evaluations")
    public String redirectToEvaluations() {
        return "redirect:/admin/evaluations";
    }

    @GetMapping("/dashboard")
    public String redirectToDashboard() {
        return "redirect:/admin/dashboard";
    }
}