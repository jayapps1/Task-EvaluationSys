package com.evaluationsys.taskevaluationsys.service.staff;

import com.evaluationsys.taskevaluationsys.dtoresponse.TaskDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.Task;
import com.evaluationsys.taskevaluationsys.entity.TaskAssignment;
import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import com.evaluationsys.taskevaluationsys.repository.TaskAssignmentRepository;
import com.evaluationsys.taskevaluationsys.repository.TaskRepository;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
import com.evaluationsys.taskevaluationsys.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    /**
     * Get all active tasks for a staff member
     * Shows: ASSIGNED, INITIATED, IN_PROGRESS, PENDING_REVIEW
     * Excludes: APPROVED, COMPLETED, PENDING_APPROVAL
     */
    public List<TaskDTOResponse> getAssignedTasks(Long staffCode) {
        User staff = userRepository.findByStaffCode(staffCode)
                .orElseThrow(() -> new RuntimeException("Staff not found with code: " + staffCode));

        System.out.println("=== getAssignedTasks called for staff: " + staffCode);
        System.out.println("Staff name: " + staff.getFirstName() + " " + staff.getOtherName());

        List<TaskAssignment> assignments = assignmentRepository.findByAssignUser_StaffCode(staffCode);
        System.out.println("Total assignments found: " + assignments.size());

        List<TaskDTOResponse> result = assignments.stream()
                .map(TaskAssignment::getTask)
                .filter(task -> {
                    boolean show = task.getTaskStatus() != TaskStatus.APPROVED
                            && task.getTaskStatus() != TaskStatus.COMPLETED
                            && task.getTaskStatus() != TaskStatus.PENDING_APPROVAL;
                    if (show) {
                        System.out.println("✓ Including task: " + task.getTaskCode() + " | Status: " + task.getTaskStatus());
                    } else {
                        System.out.println("✗ Excluding task: " + task.getTaskCode() + " | Status: " + task.getTaskStatus());
                    }
                    return show;
                })
                .map(this::convertToTaskDTOResponse)
                .collect(Collectors.toList());

        System.out.println("Returning " + result.size() + " active tasks");
        return result;
    }

    /**
     * Get ALL tasks (including completed) assigned to a staff member
     */
    public List<TaskDTOResponse> getAllAssignedTasks(Long staffCode) {
        List<TaskAssignment> assignments = assignmentRepository.findByAssignUser_StaffCode(staffCode);
        return assignments.stream()
                .map(TaskAssignment::getTask)
                .map(this::convertToTaskDTOResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks by specific status for a staff member
     */
    public List<TaskDTOResponse> getTasksByStatus(Long staffCode, TaskStatus status) {
        List<TaskAssignment> assignments = assignmentRepository.findByStaffCodeAndStatus(staffCode, status);
        return assignments.stream()
                .map(TaskAssignment::getTask)
                .map(this::convertToTaskDTOResponse)
                .collect(Collectors.toList());
    }

    /**
     * Accept a task - ASSIGNED → INITIATED
     */
    @Transactional
    public boolean acceptTask(Long taskId, Long staffCode) {
        try {
            if (!isTaskAssignedToStaff(taskId, staffCode)) {
                return false;
            }
            return taskService.staffAcceptTask(taskId, staffCode);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Start a task - INITIATED → IN_PROGRESS
     */
    @Transactional
    public boolean startTask(Long taskId, Long staffCode) {
        try {
            if (!isTaskAssignedToStaff(taskId, staffCode)) {
                return false;
            }
            return taskService.staffStartProgress(taskId, staffCode);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Staff completes task - IN_PROGRESS → PENDING_REVIEW
     */
    @Transactional
    public boolean completeTask(Long taskId, Long staffCode) {
        try {
            if (!isTaskAssignedToStaff(taskId, staffCode)) {
                System.out.println("Task " + taskId + " not assigned to staff " + staffCode);
                return false;
            }

            Task task = taskRepository.findById(taskId).orElse(null);
            if (task == null || task.getTaskStatus() != TaskStatus.IN_PROGRESS) {
                System.out.println("Task status is not IN_PROGRESS, cannot complete");
                return false;
            }

            task.setTaskStatus(TaskStatus.PENDING_REVIEW);
            taskRepository.save(task);
            updateAssignmentStatus(taskId, staffCode, TaskStatus.PENDING_REVIEW);

            System.out.println("Task " + task.getTaskCode() + " submitted for supervisor review");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Request to complete task - Alias for completeTask
     * Called by controller for clarity (Option 2)
     */
    @Transactional
    public boolean requestCompleteTask(Long taskId, Long staffCode) {
        return completeTask(taskId, staffCode);
    }

    /**
     * Check if a task is assigned to a specific staff member
     */
    public boolean isTaskAssignedToStaff(Long taskId, Long staffCode) {
        return assignmentRepository.existsByTask_TaskIdAndAssignUser_StaffCode(taskId, staffCode);
    }

    /**
     * Get task statistics for a staff member
     */
    public StaffTaskStatistics getTaskStatistics(Long staffCode) {
        List<Task> allTasks = getAllAssignedTasksRaw(staffCode);

        StaffTaskStatistics stats = new StaffTaskStatistics();
        stats.setTotalTasks(allTasks.size());
        stats.setPendingTasks((int) allTasks.stream()
                .filter(t -> t.getTaskStatus() == TaskStatus.ASSIGNED ||
                        t.getTaskStatus() == TaskStatus.INITIATED).count());
        stats.setInProgressTasks((int) allTasks.stream()
                .filter(t -> t.getTaskStatus() == TaskStatus.IN_PROGRESS).count());
        stats.setPendingReviewTasks((int) allTasks.stream()
                .filter(t -> t.getTaskStatus() == TaskStatus.PENDING_REVIEW).count());
        stats.setCompletedTasks((int) allTasks.stream()
                .filter(t -> t.getTaskStatus() == TaskStatus.APPROVED ||
                        t.getTaskStatus() == TaskStatus.COMPLETED).count());

        if (stats.getTotalTasks() > 0) {
            stats.setCompletionRate((stats.getCompletedTasks() * 100.0) / stats.getTotalTasks());
        }

        return stats;
    }

    /**
     * Get overdue tasks
     */
    public List<TaskDTOResponse> getOverdueTasks(Long staffCode) {
        LocalDateTime now = LocalDateTime.now();
        List<TaskAssignment> assignments = assignmentRepository.findByAssignUser_StaffCode(staffCode);

        return assignments.stream()
                .map(TaskAssignment::getTask)
                .filter(task -> task.getDeadline() != null &&
                        task.getDeadline().isBefore(now) &&
                        task.getTaskStatus() != TaskStatus.APPROVED &&
                        task.getTaskStatus() != TaskStatus.COMPLETED)
                .map(this::convertToTaskDTOResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get task by task code
     */
    public Optional<TaskDTOResponse> getTaskByCode(String taskCode, Long staffCode) {
        Optional<Task> task = taskRepository.findByTaskCode(taskCode);
        if (task.isPresent() && isTaskAssignedToStaff(task.get().getTaskId(), staffCode)) {
            return Optional.of(convertToTaskDTOResponse(task.get()));
        }
        return Optional.empty();
    }

    /**
     * Get staff information
     */
    public Optional<User> getStaffInfo(Long staffCode) {
        return userRepository.findByStaffCode(staffCode);
    }

    /**
     * Get current tasks (ASSIGNED, INITIATED, IN_PROGRESS, PENDING_REVIEW)
     */
    public List<TaskDTOResponse> getCurrentTasks(Long staffCode) {
        List<TaskAssignment> assignments = assignmentRepository.findCurrentTasksByStaffCode(staffCode);
        return assignments.stream()
                .map(TaskAssignment::getTask)
                .map(this::convertToTaskDTOResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get active assignments (excluding APPROVED, COMPLETED, REJECTED)
     */
    public List<TaskDTOResponse> getActiveTasks(Long staffCode) {
        List<TaskAssignment> assignments = assignmentRepository.findActiveAssignmentsByStaffCode(staffCode);
        return assignments.stream()
                .map(TaskAssignment::getTask)
                .map(this::convertToTaskDTOResponse)
                .collect(Collectors.toList());
    }

    /**
     * DEBUG METHOD
     */
    public Map<String, Object> debugStaffAssignments(Long staffCode) {
        Map<String, Object> debug = new HashMap<>();

        Optional<User> staffOpt = userRepository.findByStaffCode(staffCode);
        if (staffOpt.isEmpty()) {
            debug.put("error", "Staff not found with code: " + staffCode);
            return debug;
        }

        User staff = staffOpt.get();
        debug.put("staffCode", staffCode);
        debug.put("staffName", staff.getFirstName() + " " + staff.getOtherName());
        debug.put("staffEmail", staff.getEmail());
        debug.put("staffRole", staff.getRole() != null ? staff.getRole().name() : "null");

        List<TaskAssignment> assignments = assignmentRepository.findByAssignUser_StaffCode(staffCode);
        debug.put("totalAssignments", assignments.size());

        List<Map<String, Object>> assignmentList = new ArrayList<>();
        for (TaskAssignment assignment : assignments) {
            Map<String, Object> assignInfo = new HashMap<>();
            Task task = assignment.getTask();
            assignInfo.put("taskId", task.getTaskId());
            assignInfo.put("taskCode", task.getTaskCode());
            assignInfo.put("taskDescription", task.getDescription());
            assignInfo.put("taskStatus", task.getTaskStatus() != null ? task.getTaskStatus().name() : "null");
            assignInfo.put("taskQuarter", task.getQuarter() != null ? task.getQuarter().name() : "null");
            assignInfo.put("taskYear", task.getYear());
            assignInfo.put("deadline", task.getDeadline() != null ? task.getDeadline().toString() : "null");
            assignInfo.put("assignmentStatus", assignment.getStatus() != null ? assignment.getStatus().name() : "null");
            assignInfo.put("assignedAt", assignment.getAssignedAt() != null ? assignment.getAssignedAt().toString() : "null");
            assignmentList.add(assignInfo);
        }
        debug.put("assignments", assignmentList);

        Map<String, Long> statusCount = assignments.stream()
                .map(a -> a.getTask().getTaskStatus())
                .filter(s -> s != null)
                .collect(Collectors.groupingBy(TaskStatus::name, Collectors.counting()));
        debug.put("statusCounts", statusCount);

        return debug;
    }

    // ===============================
    // PRIVATE HELPER METHODS
    // ===============================

    private void updateAssignmentStatus(Long taskId, Long staffCode, TaskStatus status) {
        List<TaskAssignment> assignments = assignmentRepository.findByTask_TaskId(taskId);
        for (TaskAssignment assignment : assignments) {
            if (assignment.getAssignUser().getStaffCode().equals(staffCode)) {
                assignment.setStatus(status);
                assignmentRepository.save(assignment);
                break;
            }
        }
    }

    private List<Task> getAllAssignedTasksRaw(Long staffCode) {
        List<TaskAssignment> assignments = assignmentRepository.findByAssignUser_StaffCode(staffCode);
        return assignments.stream()
                .map(TaskAssignment::getTask)
                .collect(Collectors.toList());
    }

    private TaskDTOResponse convertToTaskDTOResponse(Task task) {
        TaskDTOResponse response = new TaskDTOResponse();
        response.setTaskId(task.getTaskId());
        response.setTaskCode(task.getTaskCode());
        response.setDescription(task.getDescription());
        response.setTaskStatus(task.getTaskStatus());
        response.setDeadline(task.getDeadline());
        response.setQuarter(task.getQuarter());
        response.setYear(task.getYear());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        if (task.getSupervisor() != null) {
            response.setSupervisorCode(String.valueOf(task.getSupervisor().getSupervisorId()));
            if (task.getSupervisor().getUser() != null) {
                String supervisorName = (task.getSupervisor().getUser().getFirstName() != null ?
                        task.getSupervisor().getUser().getFirstName() : "") +
                        " " + (task.getSupervisor().getUser().getOtherName() != null ?
                        task.getSupervisor().getUser().getOtherName() : "");
                response.setSupervisorName(supervisorName.trim());
            }
        }

        if (task.getCreatedBy() != null) {
            response.setCreatedByStaffCode(task.getCreatedBy().getStaffCode());
            String createdByName = (task.getCreatedBy().getFirstName() != null ?
                    task.getCreatedBy().getFirstName() : "") +
                    " " + (task.getCreatedBy().getOtherName() != null ?
                    task.getCreatedBy().getOtherName() : "");
            response.setCreatedByName(createdByName.trim());
        }

        return response;
    }
}