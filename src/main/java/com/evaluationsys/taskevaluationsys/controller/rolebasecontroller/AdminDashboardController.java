package com.evaluationsys.taskevaluationsys.controller.rolebasecontroller;

import com.evaluationsys.taskevaluationsys.dtoresponse.EvaluationDTOResponse;
import com.evaluationsys.taskevaluationsys.dtoresponse.TaskAssignmentDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.*;
import com.evaluationsys.taskevaluationsys.entity.enums.Role;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import com.evaluationsys.taskevaluationsys.security.CustomUserDetails;
import com.evaluationsys.taskevaluationsys.service.admin.AdminDashboardService;
import com.evaluationsys.taskevaluationsys.service.admin.AdminStatistics;
import com.evaluationsys.taskevaluationsys.service.admin.AdminTaskService;
import com.evaluationsys.taskevaluationsys.service.admin.StaffEvaluationService;
import com.evaluationsys.taskevaluationsys.repository.TaskAssignmentRepository;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardController.class);

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Autowired
    private AdminTaskService adminTaskService;

    @Autowired
    private StaffEvaluationService staffEvaluationService;

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private UserRepository userRepository;

    // =========================
    // DASHBOARD HOME
    // =========================
    @GetMapping({"/", "/dashboard", "/home"})
    public String dashboard(Model model) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            User admin = userDetails.getUser();
            model.addAttribute("adminName", admin.getFirstName() + " " + admin.getOtherName());
            model.addAttribute("adminEmail", admin.getEmail());

            AdminStatistics stats = adminDashboardService.getDashboardStatistics();
            model.addAttribute("statistics", stats);
            model.addAttribute("staffByBranch", adminDashboardService.getStaffCountByBranch());
            model.addAttribute("staffByDepartment", adminDashboardService.getStaffCountByDepartment());
            model.addAttribute("supervisorByDepartment", adminDashboardService.getSupervisorCountByDepartment());
            model.addAttribute("departmentPerformance", adminDashboardService.getDepartmentPerformance());
            model.addAttribute("branchPerformance", adminDashboardService.getBranchPerformance());
            model.addAttribute("recentAssignments", adminDashboardService.getRecentAssignments(5));

            List<TaskAssignmentDTOResponse> pendingTasks = adminTaskService.getPendingApprovalAssignments();
            model.addAttribute("pendingApprovalCount", pendingTasks.size());
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
    // EVALUATIONS PAGE
    // =========================
    @GetMapping({"/evaluations", "/evaluation-dashboard", "/eval"})
    public String evaluationsPage(Model model) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            User admin = userDetails.getUser();
            model.addAttribute("adminName", admin.getFirstName() + " " + admin.getOtherName());
            model.addAttribute("adminEmail", admin.getEmail());

            AdminStatistics stats = adminDashboardService.getDashboardStatistics();
            model.addAttribute("statistics", stats);
            model.addAttribute("staffByBranch", adminDashboardService.getStaffCountByBranch());
            model.addAttribute("staffByDepartment", adminDashboardService.getStaffCountByDepartment());
            model.addAttribute("supervisorByDepartment", adminDashboardService.getSupervisorCountByDepartment());
            model.addAttribute("departmentPerformance", adminDashboardService.getDepartmentPerformance());
            model.addAttribute("branchPerformance", adminDashboardService.getBranchPerformance());
            model.addAttribute("recentAssignments", adminDashboardService.getRecentAssignments(10));

            List<TaskAssignmentDTOResponse> pendingTasks = adminTaskService.getPendingApprovalAssignments();
            model.addAttribute("pendingApprovalCount", pendingTasks.size());
            model.addAttribute("pendingTasks", pendingTasks);
            model.addAttribute("branches", adminDashboardService.getAllBranches());
            model.addAttribute("departments", adminDashboardService.getAllDepartments());
            model.addAttribute("taskStatusCounts", adminDashboardService.getTaskCountsByStatus());
            model.addAttribute("monthlyCompletions", adminDashboardService.getMonthlyCompletionStats());
            model.addAttribute("staffPerformance", adminDashboardService.getStaffPerformanceStats());

            return "layout/admin/evaluations";

        } catch (Exception e) {
            log.error("Error loading evaluations page", e);
            model.addAttribute("error", "Failed to load evaluations: " + e.getMessage());
            return "layout/admin/evaluations";
        }
    }

    // =========================
    // STAFF EVALUATIONS PAGE
    // =========================
    @GetMapping("/staff-evaluations")
    public String staffEvaluations(Model model) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            List<Map<String, Object>> evaluations = staffEvaluationService.getAllStaffEvaluationSummary();
            model.addAttribute("evaluations", evaluations);
            model.addAttribute("branches", adminDashboardService.getAllBranches());
            model.addAttribute("departments", adminDashboardService.getAllDepartments());

            double overallAvgScore = evaluations.stream()
                    .mapToDouble(e -> (double) e.get("averageScore"))
                    .average()
                    .orElse(0.0);

            double overallCompletionRate = evaluations.stream()
                    .mapToDouble(e -> (double) e.get("completionRate"))
                    .average()
                    .orElse(0.0);

            model.addAttribute("overallAvgScore", Math.round(overallAvgScore * 100.0) / 100.0);
            model.addAttribute("overallCompletionRate", Math.round(overallCompletionRate * 100.0) / 100.0);
            model.addAttribute("totalStaff", evaluations.size());

            return "layout/admin/staff-evaluations";

        } catch (Exception e) {
            log.error("Error loading staff evaluations", e);
            model.addAttribute("error", "Failed to load evaluations: " + e.getMessage());
            return "layout/admin/staff-evaluations";
        }
    }

    // =========================
    // STAFF EVALUATIONS FILTERED
    // =========================
    @GetMapping("/staff-evaluations/filter")
    public String staffEvaluationsFiltered(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String quarter,
            Model model) {

        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            List<Map<String, Object>> evaluations = staffEvaluationService
                    .getFilteredStaffEvaluationSummary(branchId, deptId);

            model.addAttribute("evaluations", evaluations);
            model.addAttribute("branches", adminDashboardService.getAllBranches());
            model.addAttribute("departments", adminDashboardService.getAllDepartments());
            model.addAttribute("selectedBranch", branchId);
            model.addAttribute("selectedDepartment", deptId);
            model.addAttribute("selectedQuarter", quarter);

            double overallAvgScore = evaluations.stream()
                    .mapToDouble(e -> (double) e.get("averageScore"))
                    .average()
                    .orElse(0.0);

            double overallCompletionRate = evaluations.stream()
                    .mapToDouble(e -> (double) e.get("completionRate"))
                    .average()
                    .orElse(0.0);

            model.addAttribute("overallAvgScore", Math.round(overallAvgScore * 100.0) / 100.0);
            model.addAttribute("overallCompletionRate", Math.round(overallCompletionRate * 100.0) / 100.0);
            model.addAttribute("totalStaff", evaluations.size());

            return "layout/admin/staff-evaluations";

        } catch (Exception e) {
            log.error("Error loading filtered staff evaluations", e);
            model.addAttribute("error", "Failed to load evaluations: " + e.getMessage());
            return "redirect:/admin/staff-evaluations";
        }
    }

    // =========================
    // API: GET STAFF EVALUATION DETAILS
    // =========================
    @GetMapping("/staff-evaluations/{staffId}")
    @ResponseBody
    public ResponseEntity<?> getStaffEvaluationDetails(@PathVariable Long staffId) {
        try {
            Map<String, Object> details = staffEvaluationService.getStaffEvaluationDetails(staffId);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            log.error("Error getting staff evaluation details", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================
    // API: GET STAFF TASKS FOR EVALUATION - FIXED: Only returns tasks for the specific staff member
    // =========================
    @GetMapping("/api/staff-tasks")
    @ResponseBody
    public ResponseEntity<?> getStaffTasks(@RequestParam Long staffId) {
        try {
            // Verify staff exists and is actually a STAFF role
            User staff = userRepository.findByStaffId(staffId).orElse(null);
            if (staff == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Staff not found"));
            }
            if (staff.getRole() != Role.STAFF) {
                return ResponseEntity.badRequest().body(Map.of("error", "User is not a staff member"));
            }

            // Get tasks ONLY for this specific staff member
            List<TaskAssignment> assignments = taskAssignmentRepository.findByAssignUser_StaffId(staffId);

            List<Map<String, Object>> tasks = assignments.stream()
                    .filter(a -> a.getAssignUser().getStaffId().equals(staffId)) // Double filter for safety
                    .map(a -> {
                        Map<String, Object> task = new HashMap<>();
                        Task t = a.getTask();
                        task.put("taskId", t.getTaskId());
                        String taskDescription = t.getDescription() != null ? t.getDescription() : "Task #" + t.getTaskId();
                        task.put("title", taskDescription.length() > 100 ? taskDescription.substring(0, 97) + "..." : taskDescription);
                        task.put("description", t.getDescription() != null ? t.getDescription() : "");
                        task.put("dueDate", t.getDeadline());
                        task.put("taskCode", t.getTaskCode());
                        task.put("status", a.getStatus().toString());
                        task.put("staffId", a.getAssignUser().getStaffId());
                        task.put("staffName", a.getAssignUser().getFirstName() + " " + (a.getAssignUser().getOtherName() != null ? a.getAssignUser().getOtherName() : ""));
                        return task;
                    })
                    .collect(Collectors.toList());

            log.info("Found {} tasks for staff: {} (ID: {})", tasks.size(), staff.getFirstName(), staffId);

            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("Error getting staff tasks for staffId: {}", staffId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================
    // API: CREATE STAFF EVALUATION
    // =========================
    @PostMapping("/staff-evaluations/create")
    @ResponseBody
    public ResponseEntity<?> createStaffEvaluation(
            @RequestParam Long staffId,
            @RequestParam Long taskId,
            @RequestParam(required = false) Double score,
            @RequestParam String remarks,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) Integer year) {

        try {
            EvaluationDTOResponse evaluation = staffEvaluationService.createStaffEvaluation(
                    staffId, taskId, score, remarks, quarter, year);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Evaluation created successfully",
                    "evaluation", evaluation
            ));
        } catch (Exception e) {
            log.error("Error creating staff evaluation", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // =========================
    // API: UPDATE STAFF EVALUATION
    // =========================
    @PutMapping("/staff-evaluations/{evaluationId}")
    @ResponseBody
    public ResponseEntity<?> updateStaffEvaluation(
            @PathVariable Long evaluationId,
            @RequestParam Double score,
            @RequestParam String remarks) {

        try {
            java.util.Optional<EvaluationDTOResponse> evaluation = staffEvaluationService
                    .updateStaffEvaluation(evaluationId, score, remarks);

            if (evaluation.isPresent()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Evaluation updated successfully",
                        "evaluation", evaluation.get()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Evaluation not found"
                ));
            }
        } catch (Exception e) {
            log.error("Error updating staff evaluation", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // =========================
    // API: GET DEPARTMENT EVALUATION STATS
    // =========================
    @GetMapping("/api/department-evaluation-stats")
    @ResponseBody
    public ResponseEntity<?> getDepartmentEvaluationStats(
            @RequestParam(required = false) Long departmentId) {
        try {
            Map<String, Object> stats = staffEvaluationService.getDepartmentEvaluationStats(departmentId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting department evaluation stats", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================
    // API: GET QUARTERLY REPORT
    // =========================
    @GetMapping("/api/quarterly-report")
    @ResponseBody
    public ResponseEntity<?> getQuarterlyReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter) {
        try {
            List<Map<String, Object>> report = staffEvaluationService.getQuarterlyReport(year, quarter);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error getting quarterly report", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================
    // FILTERED DASHBOARD
    // =========================
    @GetMapping({"/dashboard/filter", "/filter", "/evaluations/filter"})
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

            AdminStatistics stats = adminDashboardService.getFilteredStatistics(branchId, deptId, quarter);
            model.addAttribute("statistics", stats);
            model.addAttribute("staffByBranch", adminDashboardService.getStaffCountByBranchFiltered(branchId, deptId));
            model.addAttribute("staffByDepartment", adminDashboardService.getStaffCountByDepartmentFiltered(branchId, deptId));
            model.addAttribute("supervisorByDepartment", adminDashboardService.getSupervisorCountByDepartmentFiltered(deptId));
            model.addAttribute("departmentPerformance", adminDashboardService.getDepartmentPerformanceFiltered(branchId, deptId, quarter));
            model.addAttribute("branchPerformance", adminDashboardService.getBranchPerformanceFiltered(branchId, deptId, quarter));
            model.addAttribute("recentAssignments", adminDashboardService.getRecentAssignmentsFiltered(5, branchId, deptId, quarter));

            List<TaskAssignmentDTOResponse> pendingTasks = adminTaskService.getPendingApprovalAssignments();
            model.addAttribute("pendingApprovalCount", pendingTasks.size());
            model.addAttribute("branches", adminDashboardService.getAllBranches());
            model.addAttribute("departments", adminDashboardService.getAllDepartments());
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
    // FILTERED EVALUATIONS PAGE
    // =========================
    @GetMapping("/evaluations/filter")
    public String evaluationsFiltered(
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

            AdminStatistics stats = adminDashboardService.getFilteredStatistics(branchId, deptId, quarter);
            model.addAttribute("statistics", stats);
            model.addAttribute("staffByBranch", adminDashboardService.getStaffCountByBranchFiltered(branchId, deptId));
            model.addAttribute("staffByDepartment", adminDashboardService.getStaffCountByDepartmentFiltered(branchId, deptId));
            model.addAttribute("departmentPerformance", adminDashboardService.getDepartmentPerformanceFiltered(branchId, deptId, quarter));
            model.addAttribute("branchPerformance", adminDashboardService.getBranchPerformanceFiltered(branchId, deptId, quarter));

            List<TaskAssignmentDTOResponse> pendingTasks = adminTaskService.getPendingApprovalAssignments();
            model.addAttribute("pendingTasks", pendingTasks);
            model.addAttribute("pendingApprovalCount", pendingTasks.size());
            model.addAttribute("branches", adminDashboardService.getAllBranches());
            model.addAttribute("departments", adminDashboardService.getAllDepartments());
            model.addAttribute("selectedBranch", branchId);
            model.addAttribute("selectedDepartment", deptId);
            model.addAttribute("selectedQuarter", quarter);

            return "layout/admin/evaluations";

        } catch (Exception e) {
            log.error("Error loading filtered evaluations", e);
            model.addAttribute("error", "Failed to apply filters: " + e.getMessage());
            return "redirect:/admin/evaluations";
        }
    }

    // =========================
    // PENDING APPROVALS PAGE
    // =========================
    @GetMapping({"/pending-approvals", "/approvals", "/tasks/pending"})
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
    @GetMapping({"/staff", "/users", "/employees"})
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
    @GetMapping({"/reports", "/statistics", "/analytics"})
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
    @GetMapping({"/api/statistics/filter", "/api/stats/filter"})
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
    @GetMapping({"/api/performance/departments", "/api/perf/depts"})
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

    @GetMapping({"/api/performance/branches", "/api/perf/branches"})
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
    @PostMapping({"/assignment/{taskId}/approve", "/task/{taskId}/approve"})
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

    @PostMapping({"/assignment/{taskId}/reject", "/task/{taskId}/reject"})
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
    @PostMapping({"/staff/{staffId}/update", "/user/{staffId}/update", "/employee/{staffId}/update"})
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
    // HELPER METHODS
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