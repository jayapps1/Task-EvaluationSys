package com.evaluationsys.taskevaluationsys.controller.rolebasecontroller;

import com.evaluationsys.taskevaluationsys.dtoresponse.TaskAssignmentDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.*;
import com.evaluationsys.taskevaluationsys.security.CustomUserDetails;
import com.evaluationsys.taskevaluationsys.service.admin.AdminDashboardService;
import com.evaluationsys.taskevaluationsys.service.admin.AdminStatistics;
import com.evaluationsys.taskevaluationsys.service.admin.AdminTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardController.class);

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Autowired
    private AdminTaskService adminTaskService;

    // =========================
    // DASHBOARD HOME
    // =========================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            User admin = userDetails.getUser();
            model.addAttribute("adminName", admin.getFirstName() + " " + admin.getOtherName());
            model.addAttribute("adminEmail", admin.getEmail());

            // Statistics
            AdminStatistics stats = adminDashboardService.getDashboardStatistics();
            model.addAttribute("statistics", stats);

            // Staff counts by branch and department
            model.addAttribute("staffByBranch", adminDashboardService.getStaffCountByBranch());
            model.addAttribute("staffByDepartment", adminDashboardService.getStaffCountByDepartment());
            model.addAttribute("supervisorByDepartment", adminDashboardService.getSupervisorCountByDepartment());

            // Performance Metrics
            model.addAttribute("departmentPerformance", adminDashboardService.getDepartmentPerformance());
            model.addAttribute("branchPerformance", adminDashboardService.getBranchPerformance());

            // Recent activities
            model.addAttribute("recentAssignments", adminDashboardService.getRecentAssignments(5));

            List<TaskAssignmentDTOResponse> pendingTasks = adminTaskService.getPendingApprovalAssignments();
            model.addAttribute("pendingApprovalCount", pendingTasks.size());

            // Branches and Departments for filters
            model.addAttribute("branches", adminDashboardService.getAllBranches());
            model.addAttribute("departments", adminDashboardService.getAllDepartments());

            return "layout/admin/index";

        } catch (Exception e) {
            log.error("Error loading admin dashboard", e);
            model.addAttribute("error", "Failed to load dashboard: " + e.getMessage());
            return "layout/admin/index";
        }
    }

    // =========================
    // FILTERED DASHBOARD
    // =========================
    @GetMapping("/dashboard/filter")
    public String dashboardFiltered(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String quarter,
            Model model) {

        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            User admin = userDetails.getUser();
            model.addAttribute("adminName", admin.getFirstName() + " " + admin.getOtherName());
            model.addAttribute("adminEmail", admin.getEmail());

            // Get filtered statistics
            AdminStatistics stats = adminDashboardService.getFilteredStatistics(branchId, deptId, quarter);
            model.addAttribute("statistics", stats);

            // Get filtered staff counts
            model.addAttribute("staffByBranch", adminDashboardService.getStaffCountByBranchFiltered(branchId, deptId));
            model.addAttribute("staffByDepartment", adminDashboardService.getStaffCountByDepartmentFiltered(branchId, deptId));
            model.addAttribute("supervisorByDepartment", adminDashboardService.getSupervisorCountByDepartmentFiltered(deptId));

            // Filtered Performance Metrics
            model.addAttribute("departmentPerformance", adminDashboardService.getDepartmentPerformanceFiltered(branchId, deptId, quarter));
            model.addAttribute("branchPerformance", adminDashboardService.getBranchPerformanceFiltered(branchId, deptId, quarter));

            // Recent assignments (filtered)
            model.addAttribute("recentAssignments", adminDashboardService.getRecentAssignmentsFiltered(5, branchId, deptId, quarter));

            List<TaskAssignmentDTOResponse> pendingTasks = adminTaskService.getPendingApprovalAssignments();
            model.addAttribute("pendingApprovalCount", pendingTasks.size());

            // Branches and Departments for filters
            model.addAttribute("branches", adminDashboardService.getAllBranches());
            model.addAttribute("departments", adminDashboardService.getAllDepartments());

            // Add selected filter values to preselect in dropdowns
            model.addAttribute("selectedBranch", branchId);
            model.addAttribute("selectedDepartment", deptId);
            model.addAttribute("selectedQuarter", quarter);

            return "layout/admin/index";

        } catch (Exception e) {
            log.error("Error loading filtered admin dashboard", e);
            model.addAttribute("error", "Failed to apply filters: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    // =========================
    // PENDING APPROVALS PAGE
    // =========================
    @GetMapping("/pending-approvals")
    public String pendingApprovals(Model model) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            List<TaskAssignmentDTOResponse> pendingTasks = adminTaskService.getPendingApprovalAssignments();
            model.addAttribute("pendingTasks", pendingTasks);
            model.addAttribute("pendingCount", pendingTasks.size());

            return "layout/admin/pending-approvals";
        } catch (Exception e) {
            log.error("Error loading pending approvals", e);
            model.addAttribute("error", "Failed to load pending approvals: " + e.getMessage());
            return "layout/admin/pending-approvals";
        }
    }

    // =========================
    // STAFF MANAGEMENT PAGE
    // =========================
    @GetMapping("/staff")
    public String staffManagement(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long deptId,
            Model model) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            List<User> staffList;
            if (branchId != null || deptId != null) {
                staffList = adminDashboardService.getFilteredStaff(branchId, deptId);
            } else {
                staffList = adminDashboardService.getAllStaff();
            }

            model.addAttribute("staffList", staffList);
            model.addAttribute("branches", adminDashboardService.getAllBranches());
            model.addAttribute("departments", adminDashboardService.getAllDepartments());
            model.addAttribute("selectedBranch", branchId);
            model.addAttribute("selectedDepartment", deptId);

            return "layout/admin/staff";
        } catch (Exception e) {
            log.error("Error loading staff management", e);
            model.addAttribute("error", "Failed to load staff: " + e.getMessage());
            return "layout/admin/staff";
        }
    }

    // =========================
    // REPORTS PAGE
    // =========================
    @GetMapping("/reports")
    public String reports(Model model) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            model.addAttribute("taskStatusCounts", adminDashboardService.getTaskCountsByStatus());
            model.addAttribute("monthlyCompletions", adminDashboardService.getMonthlyCompletionStats());
            model.addAttribute("staffPerformance", adminDashboardService.getStaffPerformanceStats());
            model.addAttribute("departmentPerformance", adminDashboardService.getDepartmentPerformance());
            model.addAttribute("branchPerformance", adminDashboardService.getBranchPerformance());

            return "layout/admin/reports";
        } catch (Exception e) {
            log.error("Error loading reports", e);
            model.addAttribute("error", "Failed to load reports: " + e.getMessage());
            return "layout/admin/reports";
        }
    }

    // =========================
    // API: GET FILTERED STATISTICS (AJAX)
    // =========================
    @GetMapping("/api/statistics/filter")
    @ResponseBody
    public ResponseEntity<?> getFilteredStatistics(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String quarter) {

        try {
            AdminStatistics stats = adminDashboardService.getFilteredStatistics(branchId, deptId, quarter);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting filtered statistics", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================
    // API: GET PERFORMANCE DATA
    // =========================
    @GetMapping("/api/performance/departments")
    @ResponseBody
    public ResponseEntity<?> getDepartmentPerformance(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String quarter) {

        try {
            List<Map<String, Object>> performance;
            if (branchId != null || deptId != null || (quarter != null && !"ALL".equals(quarter))) {
                performance = adminDashboardService.getDepartmentPerformanceFiltered(branchId, deptId, quarter);
            } else {
                performance = adminDashboardService.getDepartmentPerformance();
            }
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            log.error("Error getting department performance", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/performance/branches")
    @ResponseBody
    public ResponseEntity<?> getBranchPerformance(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String quarter) {

        try {
            List<Map<String, Object>> performance;
            if (branchId != null || deptId != null || (quarter != null && !"ALL".equals(quarter))) {
                performance = adminDashboardService.getBranchPerformanceFiltered(branchId, deptId, quarter);
            } else {
                performance = adminDashboardService.getBranchPerformance();
            }
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            log.error("Error getting branch performance", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================
    // APPROVE/REJECT
    // =========================
    @PostMapping("/assignment/{taskId}/approve")
    @ResponseBody
    public ResponseEntity<Map<String, String>> approveAssignment(
            @PathVariable Long taskId,
            @RequestParam Long staffId) {

        try {
            boolean success = adminTaskService.approveAssignment(taskId, staffId);

            Map<String, String> response = new HashMap<>();
            if (success) {
                response.put("status", "success");
                response.put("message", "Task approved successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "failed");
                response.put("message", "Failed to approve task - invalid status or permissions");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error approving assignment", e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/assignment/{taskId}/reject")
    @ResponseBody
    public ResponseEntity<Map<String, String>> rejectAssignment(
            @PathVariable Long taskId,
            @RequestParam Long staffId,
            @RequestParam String reason) {

        try {
            boolean success = adminTaskService.rejectAssignment(taskId, staffId, reason);

            Map<String, String> response = new HashMap<>();
            if (success) {
                response.put("status", "success");
                response.put("message", "Task rejected");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "failed");
                response.put("message", "Failed to reject task - invalid status or permissions");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error rejecting assignment", e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    // =========================
    // STAFF UPDATE
    // =========================
    @PostMapping("/staff/{staffId}/update")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateStaff(
            @PathVariable Long staffId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long departmentId) {

        try {
            boolean success = adminDashboardService.updateStaffInfo(staffId, branchId, departmentId);

            Map<String, String> response = new HashMap<>();
            if (success) {
                response.put("status", "success");
                response.put("message", "Staff updated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "failed");
                response.put("message", "Failed to update staff - staff not found");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error updating staff", e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
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