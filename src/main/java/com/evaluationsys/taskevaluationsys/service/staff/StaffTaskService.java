package com.evaluationsys.taskevaluationsys.service.staff;

import com.evaluationsys.taskevaluationsys.dtoresponse.TaskAssignmentDTOResponse;
import com.evaluationsys.taskevaluationsys.dtoresponse.TaskDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.Task;
import com.evaluationsys.taskevaluationsys.entity.TaskAssignment;
import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import com.evaluationsys.taskevaluationsys.repository.TaskAssignmentRepository;
import com.evaluationsys.taskevaluationsys.repository.TaskRepository;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
import com.evaluationsys.taskevaluationsys.service.TaskAssignmentService;
import com.evaluationsys.taskevaluationsys.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StaffTaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskAssignmentRepository assignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskAssignmentService taskAssignmentService;  // ✅ ADDED

    // =========================
    // ✅ GET STAFF ASSIGNMENTS (Returns TaskAssignmentDTOResponse)
    // =========================
    public List<TaskAssignmentDTOResponse> getStaffAssignments(Long staffId) {
        List<TaskAssignment> assignments = assignmentRepository.findByAssignUser_StaffId(staffId);

        System.out.println("=== getStaffAssignments called for staffId: " + staffId);
        System.out.println("Total assignments found: " + assignments.size());

        return assignments.stream()
                .map(this::convertToAssignmentResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // =========================
    // ✅ GET ACTIVE ASSIGNMENTS (Excluding APPROVED, REJECTED)
    // =========================
    public List<TaskAssignmentDTOResponse> getActiveAssignments(Long staffId) {
        List<TaskAssignment> assignments = assignmentRepository.findActiveAssignmentsByStaffId(staffId);

        return assignments.stream()
                .map(this::convertToAssignmentResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // =========================
    // ✅ GET ASSIGNMENTS BY STATUS
    // =========================
    public List<TaskAssignmentDTOResponse> getAssignmentsByStatus(Long staffId, TaskStatus status) {
        List<TaskAssignment> assignments = assignmentRepository.findByStaffIdAndStatus(staffId, status);

        return assignments.stream()
                .map(this::convertToAssignmentResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // =========================
    // ✅ GET OVERDUE ASSIGNMENTS
    // =========================
    public List<TaskAssignmentDTOResponse> getOverdueAssignments(Long staffId) {
        LocalDateTime now = LocalDateTime.now();
        List<TaskAssignment> assignments = assignmentRepository.findByAssignUser_StaffId(staffId);

        return assignments.stream()
                .filter(a -> a.getTask().getDeadline() != null &&
                        a.getTask().getDeadline().isBefore(now) &&
                        a.getStatus() != TaskStatus.APPROVED)
                .map(this::convertToAssignmentResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // =========================
    // ✅ ACCEPT ASSIGNMENT (ASSIGNED → INITIATED)
    // =========================
    @Transactional
    public boolean acceptAssignment(Long taskId, Long staffId) {
        try {
            if (!isAssignmentAssignedToStaff(taskId, staffId)) {
                System.out.println("Assignment not found for taskId: " + taskId + " and staffId: " + staffId);
                return false;
            }

            Optional<TaskAssignmentDTOResponse> result =
                    taskAssignmentService.updateMyAssignmentStatus(taskId, staffId, TaskStatus.INITIATED);

            return result.isPresent();
        } catch (Exception e) {
            System.err.println("Error accepting assignment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // ✅ START ASSIGNMENT (INITIATED → IN_PROGRESS)
    // =========================
    @Transactional
    public boolean startAssignment(Long taskId, Long staffId) {
        try {
            if (!isAssignmentAssignedToStaff(taskId, staffId)) {
                System.out.println("Assignment not found for taskId: " + taskId + " and staffId: " + staffId);
                return false;
            }

            Optional<TaskAssignmentDTOResponse> result =
                    taskAssignmentService.updateMyAssignmentStatus(taskId, staffId, TaskStatus.IN_PROGRESS);

            return result.isPresent();
        } catch (Exception e) {
            System.err.println("Error starting assignment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // ✅ COMPLETE ASSIGNMENT (IN_PROGRESS → COMPLETED)
    // =========================
    @Transactional
    public boolean completeAssignment(Long taskId, Long staffId) {
        try {
            if (!isAssignmentAssignedToStaff(taskId, staffId)) {
                System.out.println("Assignment not found for taskId: " + taskId + " and staffId: " + staffId);
                return false;
            }

            // First update to COMPLETED
            Optional<TaskAssignmentDTOResponse> result =
                    taskAssignmentService.updateMyAssignmentStatus(taskId, staffId, TaskStatus.COMPLETED);

            if (result.isPresent()) {
                // Then update to PENDING_REVIEW (submits for supervisor approval)
                taskAssignmentService.updateMyAssignmentStatus(taskId, staffId, TaskStatus.PENDING_REVIEW);
                return true;
            }

            return false;
        } catch (Exception e) {
            System.err.println("Error completing assignment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // ✅ CHECK IF ASSIGNMENT EXISTS
    // =========================
    public boolean isAssignmentAssignedToStaff(Long taskId, Long staffId) {
        return assignmentRepository.existsByTask_TaskIdAndAssignUser_StaffId(taskId, staffId);
    }

    // =========================
    // ✅ STATISTICS FOR ASSIGNMENTS
    // =========================

    public StaffTaskStatistics getAssignmentStatistics(Long staffId) {
        List<TaskAssignment> assignments = assignmentRepository.findByAssignUser_StaffId(staffId);

        StaffTaskStatistics stats = new StaffTaskStatistics();
        stats.setTotalTasks(assignments.size());

        // ✅ Pending Tasks = ASSIGNED + INITIATED (according to your definition)
        stats.setPendingTasks((int) assignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.ASSIGNED ||
                        a.getStatus() == TaskStatus.INITIATED)
                .count());

        // ✅ In Progress = IN_PROGRESS
        stats.setInProgressTasks((int) assignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.IN_PROGRESS)
                .count());

        // ✅ Pending Review = PENDING_REVIEW (waiting for supervisor)
        stats.setPendingReviewTasks((int) assignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.PENDING_REVIEW)
                .count());

        // ✅ Completed = APPROVED + COMPLETED
        stats.setCompletedTasks((int) assignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.APPROVED ||
                        a.getStatus() == TaskStatus.COMPLETED)
                .count());

        // ✅ Completion Rate
        if (stats.getTotalTasks() > 0) {
            stats.setCompletionRate((stats.getCompletedTasks() * 100.0) / stats.getTotalTasks());
        }

        return stats;
    }

    // =========================
    // ✅ DEBUG METHOD
    // =========================
    public Map<String, Object> debugStaffAssignments(Long staffId) {
        Map<String, Object> debug = new HashMap<>();

        Optional<User> staffOpt = userRepository.findById(staffId);
        if (staffOpt.isEmpty()) {
            debug.put("error", "Staff not found with ID: " + staffId);
            return debug;
        }

        User staff = staffOpt.get();
        debug.put("staffId", staffId);
        debug.put("staffCode", staff.getStaffCode());
        debug.put("staffName", staff.getFirstName() + " " + staff.getOtherName());

        List<TaskAssignment> assignments = assignmentRepository.findByAssignUser_StaffId(staffId);
        debug.put("totalAssignments", assignments.size());

        List<Map<String, Object>> assignmentList = new ArrayList<>();
        for (TaskAssignment a : assignments) {
            Map<String, Object> info = new HashMap<>();
            info.put("assignCode", a.getTaskAssignCode());
            info.put("taskId", a.getTask().getTaskId());
            info.put("taskCode", a.getTask().getTaskCode());
            info.put("description", a.getTask().getDescription());
            info.put("assignmentStatus", a.getStatus() != null ? a.getStatus().name() : "null");
            info.put("taskStatus", a.getTask().getTaskStatus() != null ? a.getTask().getTaskStatus().name() : "null");
            info.put("deadline", a.getTask().getDeadline() != null ? a.getTask().getDeadline().toString() : "null");
            assignmentList.add(info);
        }
        debug.put("assignments", assignmentList);

        return debug;
    }

    // =========================
    // ✅ CONVERTER: TaskAssignment → TaskAssignmentDTOResponse
    // =========================
    private TaskAssignmentDTOResponse convertToAssignmentResponse(TaskAssignment assignment) {
        if (assignment == null) return null;

        Task task = assignment.getTask();
        User staff = assignment.getAssignUser();

        if (task == null || staff == null) return null;

        String departmentName = (staff.getDepartment() != null)
                ? staff.getDepartment().getDepartmentName()
                : "N/A";

        String branchName = (staff.getBranch() != null)
                ? staff.getBranch().getBranchName()
                : "N/A";

        return new TaskAssignmentDTOResponse(
                assignment.getTaskAssignCode(),
                task.getTaskId(),
                task.getDescription(),
                task.getDeadline(),
                staff.getStaffId(),
                staff.getFirstName(),
                staff.getOtherName(),
                departmentName,
                branchName,
                assignment.getAssignedAt(),
                assignment.getStatus() != null ? assignment.getStatus().name() : null
        );
    }

    // =========================
    // LEGACY METHODS (for backward compatibility)
    // =========================
    public List<TaskDTOResponse> getAssignedTasks(Long staffCode) {
        User staff = userRepository.findByStaffCode(staffCode).orElse(null);
        if (staff == null) return List.of();

        return getStaffAssignments(staff.getStaffId()).stream()
                .map(this::convertAssignmentToTaskResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<TaskDTOResponse> getTasksByStatus(Long staffCode, TaskStatus status) {
        User staff = userRepository.findByStaffCode(staffCode).orElse(null);
        if (staff == null) return List.of();

        return getAssignmentsByStatus(staff.getStaffId(), status).stream()
                .map(this::convertAssignmentToTaskResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<TaskDTOResponse> getOverdueTasks(Long staffCode) {
        User staff = userRepository.findByStaffCode(staffCode).orElse(null);
        if (staff == null) return List.of();

        return getOverdueAssignments(staff.getStaffId()).stream()
                .map(this::convertAssignmentToTaskResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public StaffTaskStatistics getTaskStatistics(Long staffCode) {
        User staff = userRepository.findByStaffCode(staffCode).orElse(null);
        if (staff == null) return new StaffTaskStatistics();

        return getAssignmentStatistics(staff.getStaffId());
    }

    public boolean acceptTask(Long taskId, Long staffCode) {
        User staff = userRepository.findByStaffCode(staffCode).orElse(null);
        if (staff == null) return false;

        return acceptAssignment(taskId, staff.getStaffId());
    }

    public boolean startTask(Long taskId, Long staffCode) {
        User staff = userRepository.findByStaffCode(staffCode).orElse(null);
        if (staff == null) return false;

        return startAssignment(taskId, staff.getStaffId());
    }

    public boolean completeTask(Long taskId, Long staffCode) {
        User staff = userRepository.findByStaffCode(staffCode).orElse(null);
        if (staff == null) return false;

        return completeAssignment(taskId, staff.getStaffId());
    }

    public boolean isTaskAssignedToStaff(Long taskId, Long staffCode) {
        User staff = userRepository.findByStaffCode(staffCode).orElse(null);
        if (staff == null) return false;

        return isAssignmentAssignedToStaff(taskId, staff.getStaffId());
    }

    // =========================
    // CONVERTER: Assignment → TaskDTOResponse (for legacy support)
    // =========================
    private TaskDTOResponse convertAssignmentToTaskResponse(TaskAssignmentDTOResponse assignment) {
        if (assignment == null) return null;

        TaskDTOResponse response = new TaskDTOResponse();
        response.setTaskId(assignment.getTaskId());
        response.setTaskCode(assignment.getTaskAssignCode()); // Note: using assign code as task code for display
        response.setDescription(assignment.getTaskDescription());
        response.setTaskStatus(TaskStatus.valueOf(assignment.getStatus()));
        response.setDeadline(assignment.getTaskDeadline());
        response.setCreatedByStaffCode(assignment.getStaffId());
        response.setCreatedByName(assignment.getStaffFullName());

        return response;
    }
}