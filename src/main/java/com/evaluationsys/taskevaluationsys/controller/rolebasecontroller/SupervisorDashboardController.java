package com.evaluationsys.taskevaluationsys.controller.rolebasecontroller;

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

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        CustomUserDetails userDetails = getAuthenticatedUser();

        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        User loggedInUser = userDetails.getUser();

        // Get statistics
        SupervisorStatistics stats = supervisorTaskService.getSupervisorStatistics(loggedInUser);
        model.addAttribute("statistics", stats);

        // Get pending review tasks
        List<TaskDTOResponse> pendingReviews = supervisorTaskService.getPendingReviewTasks(loggedInUser);
        model.addAttribute("pendingReviews", pendingReviews);

        // Get in progress tasks
        List<TaskDTOResponse> inProgressTasks = supervisorTaskService.getInProgressTasks(loggedInUser);
        model.addAttribute("inProgressTasks", inProgressTasks);

        model.addAttribute("supervisorName", loggedInUser.getFirstName() + " " + loggedInUser.getOtherName());
        model.addAttribute("staffCode", loggedInUser.getStaffCode());

        return "supervisor/index";
    }

    @GetMapping("/tasks/pending-review")
    @ResponseBody
    public List<TaskDTOResponse> getPendingReviewTasks() {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return List.of();
        }
        return supervisorTaskService.getPendingReviewTasks(userDetails.getUser());
    }

    @GetMapping("/tasks/in-progress")
    @ResponseBody
    public List<TaskDTOResponse> getInProgressTasks() {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return List.of();
        }
        return supervisorTaskService.getInProgressTasks(userDetails.getUser());
    }

    @GetMapping("/tasks/status/{status}")
    @ResponseBody
    public List<TaskDTOResponse> getTasksByStatus(@PathVariable TaskStatus status) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return List.of();
        }
        return supervisorTaskService.getTasksByStatus(userDetails.getUser(), status);
    }

    @PostMapping("/task/{taskId}/approve")
    @ResponseBody
    public ResponseEntity<Map<String, String>> approveTask(@PathVariable Long taskId) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = supervisorTaskService.approveTask(taskId, userDetails.getUser());

        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("status", "success");
            response.put("message", "Task approved successfully!");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "failed");
            response.put("message", "Failed to approve task");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/task/{taskId}/reject")
    @ResponseBody
    public ResponseEntity<Map<String, String>> rejectTask(@PathVariable Long taskId,
                                                          @RequestParam String reason) {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = supervisorTaskService.rejectTask(taskId, userDetails.getUser(), reason);

        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("status", "success");
            response.put("message", "Task rejected. Staff can redo the task.");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "failed");
            response.put("message", "Failed to reject task");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/statistics")
    @ResponseBody
    public SupervisorStatistics getStatistics() {
        CustomUserDetails userDetails = getAuthenticatedUser();
        if (userDetails == null) {
            return new SupervisorStatistics();
        }
        return supervisorTaskService.getSupervisorStatistics(userDetails.getUser());
    }

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