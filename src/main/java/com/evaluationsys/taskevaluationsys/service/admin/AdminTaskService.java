package com.evaluationsys.taskevaluationsys.service.admin;

import com.evaluationsys.taskevaluationsys.dtoresponse.TaskAssignmentDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.*;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import com.evaluationsys.taskevaluationsys.repository.TaskAssignmentRepository;
import com.evaluationsys.taskevaluationsys.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminTaskService {

    private static final Logger log = LoggerFactory.getLogger(AdminTaskService.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskAssignmentRepository assignmentRepository;

    // =========================
    // GET PENDING APPROVAL ASSIGNMENTS
    // =========================
    public List<TaskAssignmentDTOResponse> getPendingApprovalAssignments() {
        return assignmentRepository.findByStatus(TaskStatus.PENDING_APPROVAL).stream()
                .map(this::convertToAssignmentResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // =========================
    // ✅ ADMIN APPROVE ASSIGNMENT (Final Approval)
    // =========================
    @Transactional
    public boolean approveAssignment(Long taskId, Long staffId) {
        Optional<TaskAssignment> optionalAssignment =
                assignmentRepository.findByTaskIdAndStaffId(taskId, staffId);

        if (optionalAssignment.isEmpty()) {
            log.warn("Assignment not found for taskId: {} and staffId: {}", taskId, staffId);
            return false;
        }

        TaskAssignment assignment = optionalAssignment.get();

        if (assignment.getStatus() != TaskStatus.PENDING_APPROVAL) {
            log.warn("Assignment {} is not PENDING_APPROVAL, current status: {}",
                    assignment.getTaskAssignCode(), assignment.getStatus());
            return false;
        }

        // ✅ Approve this specific assignment
        assignment.setStatus(TaskStatus.APPROVED);
        assignmentRepository.save(assignment);

        log.info("✅ Admin approved assignment {} for staff {}", assignment.getTaskAssignCode(), staffId);

        // ✅ Check if ALL assignments for this task are APPROVED
        List<TaskAssignment> allAssignments = assignmentRepository.findByTask_TaskId(taskId);
        boolean allApproved = allAssignments.stream()
                .allMatch(a -> a.getStatus() == TaskStatus.APPROVED);

        if (allApproved) {
            Task task = assignment.getTask();
            task.setTaskStatus(TaskStatus.APPROVED);
            taskRepository.save(task);
            log.info("📋 All assignments approved for task {}, task status updated to APPROVED", taskId);
        }

        return true;
    }

    // =========================
    // ✅ ADMIN REJECT ASSIGNMENT
    // =========================
    @Transactional
    public boolean rejectAssignment(Long taskId, Long staffId, String reason) {
        Optional<TaskAssignment> optionalAssignment =
                assignmentRepository.findByTaskIdAndStaffId(taskId, staffId);

        if (optionalAssignment.isEmpty()) {
            log.warn("Assignment not found for taskId: {} and staffId: {}", taskId, staffId);
            return false;
        }

        TaskAssignment assignment = optionalAssignment.get();

        if (assignment.getStatus() != TaskStatus.PENDING_APPROVAL) {
            log.warn("Assignment {} is not PENDING_APPROVAL, current status: {}",
                    assignment.getTaskAssignCode(), assignment.getStatus());
            return false;
        }

        // ✅ Reject and send back to IN_PROGRESS
        assignment.setStatus(TaskStatus.REJECTED);
        assignmentRepository.save(assignment);

        Task task = assignment.getTask();
        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task);

        log.info("❌ Admin rejected assignment {} for staff {}. Reason: {}",
                assignment.getTaskAssignCode(), staffId, reason);

        return true;
    }

    // =========================
    // DTO CONVERTER
    // =========================
    private TaskAssignmentDTOResponse convertToAssignmentResponse(TaskAssignment a) {
        Task task = a.getTask();
        User user = a.getAssignUser();

        if (task == null || user == null) return null;

        String departmentName = (user.getDepartment() != null)
                ? user.getDepartment().getDepartmentName() : "N/A";
        String branchName = (user.getBranch() != null)
                ? user.getBranch().getBranchName() : "N/A";

        return new TaskAssignmentDTOResponse(
                a.getTaskAssignCode(),
                task.getTaskId(),
                task.getDescription(),
                task.getDeadline(),
                user.getStaffId(),
                user.getFirstName(),
                user.getOtherName(),
                departmentName,
                branchName,
                a.getAssignedAt(),
                a.getStatus() != null ? a.getStatus().name() : null
        );
    }
}