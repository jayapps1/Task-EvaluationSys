package com.evaluationsys.taskevaluationsys.controller.reports;

import com.evaluationsys.taskevaluationsys.entity.*;
import com.evaluationsys.taskevaluationsys.security.CustomUserDetails;
import com.evaluationsys.taskevaluationsys.service.admin.AdminDashboardService;
import com.evaluationsys.taskevaluationsys.service.admin.AdminStatistics;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class ReportsController {

    private static final Logger log = LoggerFactory.getLogger(ReportsController.class);

    @Autowired
    private AdminDashboardService adminDashboardService;

    // =========================
    // TASK STATUS REPORT
    // =========================
    @GetMapping("/task-status")
    public String taskStatusReport(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String quarter,
            Model model) {

        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            model.addAttribute("pageTitle", "Task Status Report");
            model.addAttribute("currentUser", userDetails.getUser());

            // Get task status counts
            Map<String, Long> taskStatusCounts;
            if (branchId != null || deptId != null || (quarter != null && !"ALL".equals(quarter))) {
                taskStatusCounts = adminDashboardService.getFilteredTaskCountsByStatus(branchId, deptId, quarter);
            } else {
                taskStatusCounts = adminDashboardService.getTaskCountsByStatus();
            }
            model.addAttribute("taskStatusCounts", taskStatusCounts);

            // Calculate total tasks
            long totalTasks = taskStatusCounts.values().stream().mapToLong(Long::longValue).sum();
            model.addAttribute("totalTasks", totalTasks);

            // Filters
            model.addAttribute("branches", adminDashboardService.getAllBranches());
            model.addAttribute("departments", adminDashboardService.getAllDepartments());
            model.addAttribute("selectedBranch", branchId);
            model.addAttribute("selectedDepartment", deptId);
            model.addAttribute("selectedQuarter", quarter);

            return "layout/reports/task-status";

        } catch (Exception e) {
            log.error("Error loading task status report", e);
            model.addAttribute("error", "Failed to load report: " + e.getMessage());
            return "layout/reports/task-status";
        }
    }

    // =========================
    // COMPLETION REPORT
    // =========================
    @GetMapping("/completion")
    public String completionReport(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String quarter,
            Model model) {

        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            model.addAttribute("pageTitle", "Completion Report");
            model.addAttribute("currentUser", userDetails.getUser());

            model.addAttribute("monthlyCompletions", adminDashboardService.getMonthlyCompletionStats());
            model.addAttribute("staffPerformance", adminDashboardService.getStaffPerformanceStats());
            model.addAttribute("departmentPerformance", adminDashboardService.getDepartmentPerformanceFiltered(branchId, deptId, quarter));
            model.addAttribute("branchPerformance", adminDashboardService.getBranchPerformanceFiltered(branchId, deptId, quarter));

            model.addAttribute("branches", adminDashboardService.getAllBranches());
            model.addAttribute("departments", adminDashboardService.getAllDepartments());
            model.addAttribute("selectedBranch", branchId);
            model.addAttribute("selectedDepartment", deptId);
            model.addAttribute("selectedQuarter", quarter);

            return "layout/reports/completion";

        } catch (Exception e) {
            log.error("Error loading completion report", e);
            model.addAttribute("error", "Failed to load report: " + e.getMessage());
            return "layout/reports/completion";
        }
    }

    // =========================
    // EXPORT DATA PAGE
    // =========================
    @GetMapping("/export")
    public String exportData(Model model) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            model.addAttribute("pageTitle", "Export Data");
            model.addAttribute("currentUser", userDetails.getUser());

            // ✅ ADD RECORD COUNTS FOR EXPORT PAGE
            AdminStatistics stats = adminDashboardService.getDashboardStatistics();
            model.addAttribute("totalStaff", stats.getTotalStaff());
            model.addAttribute("totalTasks", stats.getTotalTasks());
            model.addAttribute("totalBranches", stats.getTotalBranches());
            model.addAttribute("totalDepartments", stats.getTotalDepartments());
            model.addAttribute("totalSupervisors", stats.getTotalSupervisors());

            return "layout/reports/export";

        } catch (Exception e) {
            log.error("Error loading export page", e);
            model.addAttribute("error", "Failed to load export page: " + e.getMessage());
            return "layout/reports/export";
        }
    }

    // =========================
    // API: EXPORT STAFF DATA
    // =========================
    @GetMapping("/api/export/staff")
    public void exportStaffData(
            @RequestParam(required = false, defaultValue = "csv") String format,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) throws IOException {

        List<User> staffList = adminDashboardService.getAllStaff();

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"staff_data.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("Staff Code,First Name,Last Name,Email,Department,Branch,Role");

        for (User staff : staffList) {
            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    staff.getStaffCode() != null ? staff.getStaffCode() : "",
                    staff.getFirstName() != null ? staff.getFirstName() : "",
                    staff.getOtherName() != null ? staff.getOtherName() : "",
                    staff.getEmail() != null ? staff.getEmail() : "",
                    staff.getDepartment() != null ? staff.getDepartment().getDepartmentName() : "N/A",
                    staff.getBranch() != null ? staff.getBranch().getBranchName() : "N/A",
                    staff.getRole() != null ? staff.getRole().name() : "N/A"
            );
        }
        writer.flush();
    }

    // =========================
    // API: EXPORT TASK DATA
    // =========================
    @GetMapping("/api/export/tasks")
    public void exportTaskData(
            @RequestParam(required = false, defaultValue = "csv") String format,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) throws IOException {

        List<TaskAssignment> assignments = adminDashboardService.getAllAssignments();

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"task_data.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("Assignment Code,Task Code,Description,Staff Name,Department,Branch,Status,Assigned Date,Deadline");

        for (TaskAssignment a : assignments) {
            Task task = a.getTask();
            User staff = a.getAssignUser();

            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    a.getTaskAssignCode() != null ? a.getTaskAssignCode() : "",
                    task != null && task.getTaskCode() != null ? task.getTaskCode() : "",
                    task != null && task.getDescription() != null ? task.getDescription().replace("\"", "\"\"") : "",
                    staff != null ? staff.getFirstName() + " " + staff.getOtherName() : "",
                    staff != null && staff.getDepartment() != null ? staff.getDepartment().getDepartmentName() : "N/A",
                    staff != null && staff.getBranch() != null ? staff.getBranch().getBranchName() : "N/A",
                    a.getStatus() != null ? a.getStatus().name() : "N/A",
                    a.getAssignedAt() != null ? a.getAssignedAt().toString() : "",
                    task != null && task.getDeadline() != null ? task.getDeadline().toString() : ""
            );
        }
        writer.flush();
    }

    // =========================
    // API: EXPORT BRANCH DATA
    // =========================
    @GetMapping("/api/export/branches")
    public void exportBranchData(
            @RequestParam(required = false, defaultValue = "csv") String format,
            HttpServletResponse response) throws IOException {

        List<Branch> branches = adminDashboardService.getAllBranches();

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"branch_data.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("Branch Code,Branch Name");

        for (Branch b : branches) {
            writer.printf("\"%s\",\"%s\"%n",
                    b.getBranchCode() != null ? b.getBranchCode() : "",
                    b.getBranchName() != null ? b.getBranchName() : ""
            );
        }
        writer.flush();
    }

    // =========================
    // API: EXPORT DEPARTMENT DATA
    // =========================
    @GetMapping("/api/export/departments")
    public void exportDepartmentData(
            @RequestParam(required = false, defaultValue = "csv") String format,
            HttpServletResponse response) throws IOException {

        List<Department> departments = adminDashboardService.getAllDepartments();

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"department_data.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("Department Code,Department Name");

        for (Department d : departments) {
            writer.printf("\"%s\",\"%s\"%n",
                    d.getDepartmentCode() != null ? d.getDepartmentCode() : "",
                    d.getDepartmentName() != null ? d.getDepartmentName() : ""
            );
        }
        writer.flush();
    }

    // =========================
    // API: EXPORT SUPERVISOR DATA
    // =========================
    @GetMapping("/api/export/supervisors")
    public void exportSupervisorData(
            @RequestParam(required = false, defaultValue = "csv") String format,
            HttpServletResponse response) throws IOException {

        List<Supervisor> supervisors = adminDashboardService.getAllSupervisors();

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"supervisor_data.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("Supervisor Code,Name,Email,Department,Branch");

        for (Supervisor s : supervisors) {
            User user = s.getUser();
            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    s.getSupervisorCode() != null ? s.getSupervisorCode() : "",
                    user != null ? user.getFirstName() + " " + user.getOtherName() : "",
                    user != null && user.getEmail() != null ? user.getEmail() : "",
                    s.getDepartment() != null ? s.getDepartment().getDepartmentName() : "N/A",
                    s.getBranch() != null ? s.getBranch().getBranchName() : "N/A"
            );
        }
        writer.flush();
    }

    // =========================
    // API: EXPORT PERFORMANCE DATA
    // =========================
    @GetMapping("/api/export/performance")
    public void exportPerformanceData(
            @RequestParam(required = false, defaultValue = "csv") String format,
            HttpServletResponse response) throws IOException {

        List<Map<String, Object>> performance = adminDashboardService.getStaffPerformanceStats();

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"performance_data.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("Staff Code,Name,Department,Branch,Total Tasks,Completed Tasks,In Progress,Completion Rate");

        for (Map<String, Object> p : performance) {
            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%d,%.1f%%%n",
                    p.get("staffCode"),
                    p.get("staffName"),
                    p.get("department"),
                    p.get("branch"),
                    p.get("totalTasks"),
                    p.get("completedTasks"),
                    p.get("inProgressTasks"),
                    p.get("completionRate")
            );
        }
        writer.flush();
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