package com.evaluationsys.taskevaluationsys.controller.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DepartmentManagementController {

    @GetMapping("/dashboard/departments")
    public String departmentPage() {
        return "dashboard/department"; // your HTML file
    }
}