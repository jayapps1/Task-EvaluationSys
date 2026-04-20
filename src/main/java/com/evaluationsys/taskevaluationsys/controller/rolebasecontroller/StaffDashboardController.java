package com.evaluationsys.taskevaluationsys.controller.rolebasecontroller;

import com.evaluationsys.taskevaluationsys.dtoresponse.TaskDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.entity.Supervisor;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import com.evaluationsys.taskevaluationsys.security.CustomUserDetails;
import com.evaluationsys.taskevaluationsys.service.staff.StaffTaskService;
import com.evaluationsys.taskevaluationsys.service.staff.StaffTaskStatistics;
import com.evaluationsys.taskevaluationsys.service.SupervisorService;

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
    private SupervisorService supervisorService;

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

        System.out.println("=== STAFF DASHBOARD ===");
        System.out.println("Staff Code: " + staffCode);

        session.setAttribute("staffCode", staffCode);
        session.setAttribute("staffName",
                authenticatedUser.getFirstName() + " " + authenticatedUser.getOtherName());

        model.addAttribute("staff", authenticatedUser);
        model.addAttribute("staffCode", staffCode);

        // =========================
        // TASKS
        // =========================
        List<TaskDTOResponse> tasks = staffTaskService.getAssignedTasks(staffCode);
        model.addAttribute("tasks", tasks);

        StaffTaskStatistics stats = staffTaskService.getTaskStatistics(staffCode);
        model.addAttribute("statistics", stats);

        List<TaskDTOResponse> overdueTasks = staffTaskService.getOverdueTasks(staffCode);
        model.addAttribute("overdueCount", overdueTasks.size());
        model.addAttribute("overdueTasks", overdueTasks);

        // =========================
        // 🔥 SUPERVISOR (NEW FIX)
        // =========================
        Optional<Supervisor> supervisorOpt =
                supervisorService.findDepartmentSupervisor(authenticatedUser);

        if (supervisorOpt.isPresent()) {
            User supUser = supervisorOpt.get().getUser();

            Map<String, String> supervisorData = new HashMap<>();
            supervisorData.put("name",
                    supUser.getFirstName() + " " + supUser.getOtherName());
            supervisorData.put("phone", supUser.getPhoneNumber());

            model.addAttribute("supervisor", supervisorData);
        } else {
            model.addAttribute("supervisor", Map.of(
                    "name", "No Supervisor Assigned",
                    "phone", "N/A"
            ));
        }

        return "staff/index";
    }

    // =========================
    // TASK ACTIONS (UNCHANGED)
    // =========================
    @PostMapping("/task/{taskId}/accept")
    @ResponseBody
    public ResponseEntity<Map<String, String>> acceptTask(@PathVariable Long taskId) {

        CustomUserDetails userDetails = getAuthenticatedUser();

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", "failed",
                    "message", "User not authenticated"
            ));
        }

        Long staffCode = userDetails.getUser().getStaffCode();
        boolean success = staffTaskService.acceptTask(taskId, staffCode);

        return success
                ? ResponseEntity.ok(Map.of("status", "success", "message", "Task accepted successfully"))
                : ResponseEntity.badRequest().body(Map.of("status", "failed",
                "message", "Task may not be in ASSIGNED status"));
    }

    @PostMapping("/task/{taskId}/start")
    public ResponseEntity<Map<String, String>> startTask(@PathVariable Long taskId) {

        CustomUserDetails userDetails = getAuthenticatedUser();

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", "failed",
                    "message", "User not authenticated"
            ));
        }

        Long staffCode = userDetails.getUser().getStaffCode();
        boolean success = staffTaskService.startTask(taskId, staffCode);

        return success
                ? ResponseEntity.ok(Map.of("status", "success", "message", "Task started"))
                : ResponseEntity.badRequest().body(Map.of("status", "failed",
                "message", "Task not in INITIATED status"));
    }

    @PostMapping("/task/{taskId}/complete")
    public ResponseEntity<Map<String, String>> completeTask(@PathVariable Long taskId) {

        CustomUserDetails userDetails = getAuthenticatedUser();

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", "failed",
                    "message", "User not authenticated"
            ));
        }

        Long staffCode = userDetails.getUser().getStaffCode();
        boolean success = staffTaskService.completeTask(taskId, staffCode);

        return success
                ? ResponseEntity.ok(Map.of("status", "success", "message", "Task completed"))
                : ResponseEntity.badRequest().body(Map.of("status", "failed",
                "message", "Task not in IN_PROGRESS"));
    }

    // =========================
    // STATUS FILTER
    // =========================
    @GetMapping("/tasks/status/{status}")
    public ResponseEntity<List<TaskDTOResponse>> getTasksByStatus(@PathVariable TaskStatus status) {

        CustomUserDetails userDetails = getAuthenticatedUser();

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Long staffCode = userDetails.getUser().getStaffCode();
        return ResponseEntity.ok(staffTaskService.getTasksByStatus(staffCode, status));
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