package com.evaluationsys.taskevaluationsys.service.supervisor;

import com.evaluationsys.taskevaluationsys.dtoresponse.TaskDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.Task;
import com.evaluationsys.taskevaluationsys.entity.TaskAssignment;
import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import com.evaluationsys.taskevaluationsys.repository.SupervisorRepository;
import com.evaluationsys.taskevaluationsys.repository.TaskAssignmentRepository;
import com.evaluationsys.taskevaluationsys.repository.TaskRepository;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SupervisorTaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskAssignmentRepository assignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupervisorRepository supervisorRepository;

    /**
     * Get all tasks pending review - Supervisor sees ALL PENDING_REVIEW tasks
     * No department filtering - any supervisor can supervise any staff
     */
    public List<TaskDTOResponse> getPendingReviewTasks(User loggedInUser) {
        System.out.println("=== Supervisor " + loggedInUser.getFirstName() + " " + loggedInUser.getOtherName() + " viewing PENDING_REVIEW tasks ===");

        List<TaskAssignment> allAssignments = assignmentRepository.findAll();

        List<TaskDTOResponse> result = allAssignments.stream()
                .map(TaskAssignment::getTask)
                .filter(task -> task.getTaskStatus() == TaskStatus.PENDING_REVIEW)
                .map(this::convertToTaskDTOResponse)
                .collect(Collectors.toList());

        System.out.println("Total PENDING_REVIEW tasks to show: " + result.size());
        return result;
    }

    /**
     * Get all tasks in progress - Supervisor sees ALL IN_PROGRESS tasks
     * No department filtering - any supervisor can supervise any staff
     */
    public List<TaskDTOResponse> getInProgressTasks(User loggedInUser) {
        System.out.println("=== Supervisor " + loggedInUser.getFirstName() + " " + loggedInUser.getOtherName() + " viewing IN_PROGRESS tasks ===");

        List<TaskAssignment> allAssignments = assignmentRepository.findAll();

        List<TaskDTOResponse> result = allAssignments.stream()
                .map(TaskAssignment::getTask)
                .filter(task -> task.getTaskStatus() == TaskStatus.IN_PROGRESS)
                .map(this::convertToTaskDTOResponse)
                .collect(Collectors.toList());

        System.out.println("Total IN_PROGRESS tasks to show: " + result.size());
        return result;
    }

    /**
     * Get all tasks - Supervisor sees ALL tasks
     * No department filtering - any supervisor can supervise any staff
     */
    public List<TaskDTOResponse> getAllSupervisorTasks(User loggedInUser) {
        List<TaskAssignment> allAssignments = assignmentRepository.findAll();

        return allAssignments.stream()
                .map(TaskAssignment::getTask)
                .map(this::convertToTaskDTOResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks by status - Supervisor sees ALL tasks with given status
     */
    public List<TaskDTOResponse> getTasksByStatus(User loggedInUser, TaskStatus status) {
        List<TaskAssignment> allAssignments = assignmentRepository.findAll();

        return allAssignments.stream()
                .map(TaskAssignment::getTask)
                .filter(task -> task.getTaskStatus() == status)
                .map(this::convertToTaskDTOResponse)
                .collect(Collectors.toList());
    }

    /**
     * Supervisor approves a completed task
     * Status: PENDING_REVIEW → APPROVED
     */
    @Transactional
    public boolean approveTask(Long taskId, User loggedInUser) {
        try {
            Optional<Task> taskOpt = taskRepository.findById(taskId);
            if (taskOpt.isEmpty()) {
                System.out.println("Task not found: " + taskId);
                return false;
            }

            Task task = taskOpt.get();

            if (task.getTaskStatus() != TaskStatus.PENDING_REVIEW) {
                System.out.println("Task " + task.getTaskCode() + " is not PENDING_REVIEW. Current status: " + task.getTaskStatus());
                return false;
            }

            task.setTaskStatus(TaskStatus.APPROVED);
            taskRepository.save(task);

            updateAssignmentStatus(taskId, TaskStatus.APPROVED);

            System.out.println("Task " + task.getTaskCode() + " approved by supervisor " + loggedInUser.getStaffCode());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supervisor rejects a completed task
     * Status: PENDING_REVIEW → IN_PROGRESS (staff redoes)
     */
    @Transactional
    public boolean rejectTask(Long taskId, User loggedInUser, String rejectionReason) {
        try {
            Optional<Task> taskOpt = taskRepository.findById(taskId);
            if (taskOpt.isEmpty()) {
                System.out.println("Task not found: " + taskId);
                return false;
            }

            Task task = taskOpt.get();

            if (task.getTaskStatus() != TaskStatus.PENDING_REVIEW) {
                System.out.println("Task " + task.getTaskCode() + " is not PENDING_REVIEW. Current status: " + task.getTaskStatus());
                return false;
            }

            task.setTaskStatus(TaskStatus.IN_PROGRESS);
            taskRepository.save(task);

            updateAssignmentStatus(taskId, TaskStatus.IN_PROGRESS);

            System.out.println("Task " + task.getTaskCode() + " rejected by supervisor " + loggedInUser.getStaffCode() + ". Reason: " + rejectionReason);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get supervisor statistics - ALL tasks
     */
    public SupervisorStatistics getSupervisorStatistics(User loggedInUser) {
        List<Task> allTasks = getAllSupervisorTasksRaw(loggedInUser);

        SupervisorStatistics stats = new SupervisorStatistics();
        stats.setTotalTasks(allTasks.size());
        stats.setInitiatedTasks((int) allTasks.stream()
                .filter(t -> t.getTaskStatus() == TaskStatus.ASSIGNED ||
                        t.getTaskStatus() == TaskStatus.INITIATED).count());
        stats.setInProgressTasks((int) allTasks.stream()
                .filter(t -> t.getTaskStatus() == TaskStatus.IN_PROGRESS).count());
        stats.setPendingReviewTasks((int) allTasks.stream()
                .filter(t -> t.getTaskStatus() == TaskStatus.PENDING_REVIEW).count());
        stats.setApprovedTasks((int) allTasks.stream()
                .filter(t -> t.getTaskStatus() == TaskStatus.APPROVED).count());

        int totalReviewed = stats.getPendingReviewTasks() + stats.getApprovedTasks();
        if (totalReviewed > 0) {
            stats.setApprovalRate((stats.getApprovedTasks() * 100.0) / totalReviewed);
        }

        System.out.println("=== Statistics for Supervisor " + loggedInUser.getStaffCode() + " ===");
        System.out.println("Total Tasks: " + stats.getTotalTasks());
        System.out.println("Pending Review: " + stats.getPendingReviewTasks());
        System.out.println("In Progress: " + stats.getInProgressTasks());
        System.out.println("Approved: " + stats.getApprovedTasks());

        return stats;
    }

    // ===============================
    // PRIVATE HELPER METHODS
    // ===============================

    private void updateAssignmentStatus(Long taskId, TaskStatus status) {
        List<TaskAssignment> assignments = assignmentRepository.findByTask_TaskId(taskId);
        if (!assignments.isEmpty()) {
            TaskAssignment assignment = assignments.get(0);
            assignment.setStatus(status);
            assignmentRepository.save(assignment);
        }
    }

    private List<Task> getAllSupervisorTasksRaw(User loggedInUser) {
        List<TaskAssignment> allAssignments = assignmentRepository.findAll();
        return allAssignments.stream()
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

        if (task.getSupervisor() != null && task.getSupervisor().getUser() != null) {
            response.setSupervisorCode(task.getSupervisor().getSupervisorCode());
            String supervisorName = (task.getSupervisor().getUser().getFirstName() != null ?
                    task.getSupervisor().getUser().getFirstName() : "") +
                    " " + (task.getSupervisor().getUser().getOtherName() != null ?
                    task.getSupervisor().getUser().getOtherName() : "");
            response.setSupervisorName(supervisorName.trim());
        }

        List<TaskAssignment> assignments = assignmentRepository.findByTask_TaskId(task.getTaskId());
        if (!assignments.isEmpty()) {
            User staff = assignments.get(0).getAssignUser();
            if (staff != null) {
                response.setCreatedByStaffCode(staff.getStaffCode());
                String staffName = (staff.getFirstName() != null ? staff.getFirstName() : "") +
                        " " + (staff.getOtherName() != null ? staff.getOtherName() : "");
                response.setCreatedByName(staffName.trim());
            }
        }

        return response;
    }
}