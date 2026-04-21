package com.evaluationsys.taskevaluationsys.controller.rolebasecontroller;

import com.evaluationsys.taskevaluationsys.dtoresponse.StaffAssignmentGroupDTOResponse;
import com.evaluationsys.taskevaluationsys.dtoresponse.TaskAssignmentDTOResponse;
import com.evaluationsys.taskevaluationsys.dtoresponse.TaskDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import com.evaluationsys.taskevaluationsys.security.CustomUserDetails;
import com.evaluationsys.taskevaluationsys.service.supervisor.SupervisorStatistics;
import com.evaluationsys.taskevaluationsys.service.supervisor.SupervisorTaskService;
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
@RequestMapping("/supervisor")
public class SupervisorDashboardController {

    @Autowired
    private SupervisorTaskService supervisorTaskService;

    // =========================
    // DASHBOARD
    // =========================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        User loggedInUser = userDetails.getUser();

        // Statistics
        SupervisorStatistics stats =
                supervisorTaskService.getSupervisorStatistics(loggedInUser);

        // Pending review tasks (using TaskDTOResponse for card view)
        List<TaskDTOResponse> pendingReviews =
                supervisorTaskService.getPendingReviewTasks(loggedInUser);

        // In progress tasks (using TaskDTOResponse for card view)
        List<TaskDTOResponse> inProgressTasks =
                supervisorTaskService.getInProgressTasks(loggedInUser);

        // Grouped assignments by staff (using StaffAssignmentGroupDTOResponse)
        List<StaffAssignmentGroupDTOResponse> groupedAssignments =
                supervisorTaskService.getGroupedAssignmentsBySupervisor(loggedInUser);

        model.addAttribute("statistics", stats);
        model.addAttribute("pendingReviews", pendingReviews);
        model.addAttribute("inProgressTasks", inProgressTasks);
        model.addAttribute("groupedAssignments", groupedAssignments);

        model.addAttribute("supervisorName", safeName(loggedInUser));
        model.addAttribute("staffCode", loggedInUser.getStaffCode());

        return "supervisor/index";
    }

    // =========================
    // AJAX: PENDING REVIEW (Tasks)
    // =========================
    @GetMapping("/tasks/pending-review")
    @ResponseBody
    public List<TaskDTOResponse> getPendingReviewTasks() {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) return List.of();
        return supervisorTaskService.getPendingReviewTasks(userDetails.getUser());
    }

    // =========================
    // AJAX: PENDING REVIEW (Assignments)
    // =========================
    @GetMapping("/assignments/pending-review")
    @ResponseBody
    public List<TaskAssignmentDTOResponse> getPendingReviewAssignments() {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) return List.of();
        return supervisorTaskService.getPendingReviewAssignments(userDetails.getUser());
    }

    // =========================
    // AJAX: IN PROGRESS (Tasks)
    // =========================
    @GetMapping("/tasks/in-progress")
    @ResponseBody
    public List<TaskDTOResponse> getInProgressTasks() {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) return List.of();
        return supervisorTaskService.getInProgressTasks(userDetails.getUser());
    }

    // =========================
    // AJAX: IN PROGRESS (Assignments)
    // =========================
    @GetMapping("/assignments/in-progress")
    @ResponseBody
    public List<TaskAssignmentDTOResponse> getInProgressAssignments() {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) return List.of();
        return supervisorTaskService.getInProgressAssignments(userDetails.getUser());
    }

    // =========================
    // AJAX: GROUPED ASSIGNMENTS
    // =========================
    @GetMapping("/assignments/grouped")
    @ResponseBody
    public List<StaffAssignmentGroupDTOResponse> getGroupedAssignments() {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) return List.of();
        return supervisorTaskService.getGroupedAssignmentsBySupervisor(userDetails.getUser());
    }

    // =========================
    // AJAX: ALL ASSIGNMENTS
    // =========================
    @GetMapping("/assignments/all")
    @ResponseBody
    public List<TaskAssignmentDTOResponse> getAllAssignments() {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) return List.of();
        return supervisorTaskService.getAllSupervisorAssignments(userDetails.getUser());
    }

    // =========================
    // STATUS FILTER (Tasks)
    // =========================
    @GetMapping("/tasks/status/{status}")
    @ResponseBody
    public List<TaskDTOResponse> getTasksByStatus(@PathVariable TaskStatus status) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) return List.of();
        return supervisorTaskService.getTasksByStatus(userDetails.getUser(), status);
    }

    // =========================
    // STATUS FILTER (Assignments)
    // =========================
    @GetMapping("/assignments/status/{status}")
    @ResponseBody
    public List<TaskAssignmentDTOResponse> getAssignmentsByStatus(@PathVariable TaskStatus status) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) return List.of();
        return supervisorTaskService.getAssignmentsByStatus(userDetails.getUser(), status);
    }

    // =========================
    // APPROVE TASK
    // =========================
    @PostMapping("/task/{taskId}/approve")
    @ResponseBody
    public ResponseEntity<Map<String, String>> approveTask(@PathVariable Long taskId) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = supervisorTaskService.approveTask(taskId, userDetails.getUser());

        return buildResponse(success,
                "Task approved successfully!",
                "Failed to approve task");
    }

    // =========================
    // APPROVE ASSIGNMENT (Using composite key: taskId and staffId)
    // =========================
    @PostMapping("/assignment/approve")
    @ResponseBody
    public ResponseEntity<Map<String, String>> approveAssignment(
            @RequestParam Long taskId,
            @RequestParam Long staffId) {

        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = supervisorTaskService.approveAssignment(taskId, staffId, userDetails.getUser());

        return buildResponse(success,
                "Assignment approved successfully!",
                "Failed to approve assignment");
    }

    // =========================
    // REJECT TASK
    // =========================
    @PostMapping("/task/{taskId}/reject")
    @ResponseBody
    public ResponseEntity<Map<String, String>> rejectTask(
            @PathVariable Long taskId,
            @RequestParam String reason) {

        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = supervisorTaskService.rejectTask(taskId, userDetails.getUser(), reason);

        return buildResponse(success,
                "Task rejected. Staff can redo the task.",
                "Failed to reject task");
    }

    // =========================
    // REJECT ASSIGNMENT (Using composite key: taskId and staffId)
    // =========================
    @PostMapping("/assignment/reject")
    @ResponseBody
    public ResponseEntity<Map<String, String>> rejectAssignment(
            @RequestParam Long taskId,
            @RequestParam Long staffId,
            @RequestParam String reason) {

        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = supervisorTaskService.rejectAssignment(taskId, staffId, userDetails.getUser(), reason);

        return buildResponse(success,
                "Assignment rejected. Staff can redo the task.",
                "Failed to reject assignment");
    }

    // =========================
    // STATISTICS
    // =========================
    @GetMapping("/statistics")
    @ResponseBody
    public SupervisorStatistics getStatistics() {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return new SupervisorStatistics();
        }
        return supervisorTaskService.getSupervisorStatistics(userDetails.getUser());
    }

    // =========================
    // HELPER METHODS
    // =========================
    private CustomUserDetails getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }
        return null;
    }

    private ResponseEntity<Map<String, String>> buildResponse(
            boolean success, String okMsg, String failMsg) {

        Map<String, String> response = new HashMap<>();

        if (success) {
            response.put("status", "success");
            response.put("message", okMsg);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "failed");
            response.put("message", failMsg);
            return ResponseEntity.badRequest().body(response);
        }
    }

    private String safeName(User user) {
        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String other = user.getOtherName() != null ? user.getOtherName() : "";
        return (first + " " + other).trim();
    }
}