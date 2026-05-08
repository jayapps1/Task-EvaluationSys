package com.evaluationsys.taskevaluationsys.controller.rolebasecontroller;

import com.evaluationsys.taskevaluationsys.dtoresponse.TaskAssignmentDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.entity.Supervisor;
import com.evaluationsys.taskevaluationsys.entity.TaskAssignment;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import com.evaluationsys.taskevaluationsys.security.CustomUserDetails;
import com.evaluationsys.taskevaluationsys.service.staff.StaffTaskService;
import com.evaluationsys.taskevaluationsys.service.staff.StaffTaskStatistics;
import com.evaluationsys.taskevaluationsys.repository.TaskAssignmentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/staff")
public class StaffDashboardController {

    @Autowired
    private StaffTaskService staffTaskService;

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    // =========================
    // DASHBOARD
    // =========================
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        CustomUserDetails userDetails = getAuthenticatedUser();

        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        User authenticatedUser = userDetails.getUser();
        Long staffCode = authenticatedUser.getStaffCode();
        Long staffId = authenticatedUser.getStaffId();

        System.out.println("=== STAFF DASHBOARD ===");
        System.out.println("Staff Code: " + staffCode);
        System.out.println("Staff ID: " + staffId);

        session.setAttribute("staffCode", staffCode);
        session.setAttribute("staffId", staffId);
        session.setAttribute("staffName",
                authenticatedUser.getFirstName() + " " + authenticatedUser.getOtherName());

        model.addAttribute("staff", authenticatedUser);
        model.addAttribute("staffCode", staffCode);
        model.addAttribute("staffId", staffId);

        // =========================
        // ASSIGNMENTS
        // =========================
        List<TaskAssignmentDTOResponse> assignments = staffTaskService.getStaffAssignments(staffId);
        model.addAttribute("assignments", assignments);

        StaffTaskStatistics stats = staffTaskService.getAssignmentStatistics(staffId);
        model.addAttribute("statistics", stats);

        List<TaskAssignmentDTOResponse> overdueAssignments = staffTaskService.getOverdueAssignments(staffId);
        model.addAttribute("overdueCount", overdueAssignments.size());
        model.addAttribute("overdueAssignments", overdueAssignments);

        // =========================
        // SUPERVISOR - Get from the staff's tasks
        // =========================
        Map<String, String> supervisorData = getSupervisorFromStaffTasks(staffId);
        model.addAttribute("supervisor", supervisorData);

        return "staff/index";
    }


    /**
     * Get supervisor information from the staff's assigned tasks
     * The supervisor who approves the task is shown
     */
    private Map<String, String> getSupervisorFromStaffTasks(Long staffId) {
        Map<String, String> supervisorData = new HashMap<>();

        // Get all task assignments for this staff
        List<TaskAssignment> assignments = taskAssignmentRepository.findByAssignUser_StaffId(staffId);

        if (assignments.isEmpty()) {
            supervisorData.put("name", "No Tasks Assigned");
            supervisorData.put("phone", "N/A");
            supervisorData.put("email", "N/A");
            return supervisorData;
        }

        // Find unique supervisors by USER (staff_id), not by supervisor record ID
        Map<Long, Supervisor> uniqueSupervisorsByUser = new HashMap<>();

        for (TaskAssignment assignment : assignments) {
            if (assignment.getTask() != null && assignment.getTask().getSupervisor() != null) {
                Supervisor supervisor = assignment.getTask().getSupervisor();
                if (supervisor.getUser() != null) {
                    Long userStaffId = supervisor.getUser().getStaffId();
                    if (!uniqueSupervisorsByUser.containsKey(userStaffId)) {
                        uniqueSupervisorsByUser.put(userStaffId, supervisor);
                    }
                }
            }
        }

        if (uniqueSupervisorsByUser.isEmpty()) {
            supervisorData.put("name", "No Supervisor Assigned to Tasks");
            supervisorData.put("phone", "N/A");
            supervisorData.put("email", "N/A");
            return supervisorData;
        }

        // Get the first supervisor (all should be the same user)
        Supervisor supervisor = uniqueSupervisorsByUser.values().iterator().next();

        if (supervisor.getUser() != null) {
            User supUser = supervisor.getUser();
            String supervisorName = (supUser.getFirstName() != null ? supUser.getFirstName() : "") +
                    " " +
                    (supUser.getOtherName() != null ? supUser.getOtherName() : "");

            supervisorData.put("name", supervisorName.trim().isEmpty() ? "Supervisor" : supervisorName.trim());
            supervisorData.put("phone", supUser.getPhoneNumber() != null ? supUser.getPhoneNumber() : "N/A");
            supervisorData.put("email", supUser.getEmail() != null ? supUser.getEmail() : "N/A");

            System.out.println("Supervisor found: " + supervisorName);
            System.out.println("Supervisor phone: " + supUser.getPhoneNumber());

            // REMOVED the (+1 more) code - no longer needed
        } else {
            supervisorData.put("name", "Supervisor Found (No User Data)");
            supervisorData.put("phone", "N/A");
            supervisorData.put("email", "N/A");
        }

        return supervisorData;
    }

    // =========================
    // ASSIGNMENT ACTIONS
    // =========================
    @PostMapping("/assignment/{taskId}/accept")
    @ResponseBody
    public ResponseEntity<Map<String, String>> acceptAssignment(@PathVariable Long taskId) {

        CustomUserDetails userDetails = getAuthenticatedUser();

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", "failed",
                    "message", "User not authenticated"
            ));
        }

        Long staffId = userDetails.getUser().getStaffId();
        boolean success = staffTaskService.acceptAssignment(taskId, staffId);

        return success
                ? ResponseEntity.ok(Map.of("status", "success", "message", "Assignment accepted successfully"))
                : ResponseEntity.badRequest().body(Map.of("status", "failed",
                "message", "Assignment may not be in ASSIGNED status"));
    }

    @PostMapping("/assignment/{taskId}/start")
    @ResponseBody
    public ResponseEntity<Map<String, String>> startAssignment(@PathVariable Long taskId) {

        CustomUserDetails userDetails = getAuthenticatedUser();

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", "failed",
                    "message", "User not authenticated"
            ));
        }

        Long staffId = userDetails.getUser().getStaffId();
        boolean success = staffTaskService.startAssignment(taskId, staffId);

        return success
                ? ResponseEntity.ok(Map.of("status", "success", "message", "Assignment started"))
                : ResponseEntity.badRequest().body(Map.of("status", "failed",
                "message", "Assignment not in INITIATED status"));
    }

    @PostMapping("/assignment/{taskId}/complete")
    @ResponseBody
    public ResponseEntity<Map<String, String>> completeAssignment(@PathVariable Long taskId) {

        CustomUserDetails userDetails = getAuthenticatedUser();

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", "failed",
                    "message", "User not authenticated"
            ));
        }

        Long staffId = userDetails.getUser().getStaffId();
        boolean success = staffTaskService.completeAssignment(taskId, staffId);

        return success
                ? ResponseEntity.ok(Map.of("status", "success", "message", "Assignment submitted for review"))
                : ResponseEntity.badRequest().body(Map.of("status", "failed",
                "message", "Assignment not in IN_PROGRESS status"));
    }

    // =========================
    // STATUS FILTER
    // =========================
    @GetMapping("/assignments/status/{status}")
    @ResponseBody
    public ResponseEntity<List<TaskAssignmentDTOResponse>> getAssignmentsByStatus(@PathVariable TaskStatus status) {

        CustomUserDetails userDetails = getAuthenticatedUser();

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Long staffId = userDetails.getUser().getStaffId();
        return ResponseEntity.ok(staffTaskService.getAssignmentsByStatus(staffId, status));
    }

    // =========================
    // AUTH HELPER
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