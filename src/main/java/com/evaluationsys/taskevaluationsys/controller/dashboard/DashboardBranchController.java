package com.evaluationsys.taskevaluationsys.controller.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardBranchController {

    @GetMapping("/dashboard/branches")
    public String branchPage() {
        return "dashboard/branch"; // matches templates/dashboard/branches.html
    }
}