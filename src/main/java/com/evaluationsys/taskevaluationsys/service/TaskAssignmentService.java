package com.evaluationsys.taskevaluationsys.service;

import com.evaluationsys.taskevaluationsys.dto.TaskAssignmentDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.TaskAssignmentDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.*;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import com.evaluationsys.taskevaluationsys.repository.TaskAssignmentRepository;
import com.evaluationsys.taskevaluationsys.repository.TaskRepository;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(TaskAssignmentService.class);

    private final TaskAssignmentRepository repository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskAssignmentService(TaskAssignmentRepository repository,
                                 TaskRepository taskRepository,
                                 UserRepository userRepository) {
        this.repository = repository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // =========================
    // CREATE ASSIGNMENT BY IDs
    // =========================
    @Transactional
    public TaskAssignmentDTOResponse createAssignment(TaskAssignmentDTO dto) {
        if (dto.getTaskId() == null || dto.getStaffId() == null) {
            throw new IllegalArgumentException("Task ID and Staff ID must be provided");
        }

        TaskAssignmentId id = new TaskAssignmentId(dto.getStaffId(), dto.getTaskId());
        if (repository.existsById(id)) {
            throw new RuntimeException("Assignment already exists for this task and staff member");
        }

        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + dto.getTaskId()));

        User user = userRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getStaffId()));

        return saveAssignment(task, user);
    }

    // =========================
    // CREATE BY DESCRIPTION + STAFF CODE
    // =========================
    @Transactional
    public TaskAssignmentDTOResponse createAssignmentByDescAndCode(String taskDescription, Long staffCode) {
        if (taskDescription == null || staffCode == null || taskDescription.isBlank()) {
            throw new IllegalArgumentException("Task description and staff code must be provided");
        }

        Task task = taskRepository.findByDescription(taskDescription)
                .orElseThrow(() -> new RuntimeException("Task not found with description: " + taskDescription));

        User user = userRepository.findByStaffCode(staffCode)
                .orElseThrow(() -> new RuntimeException("User not found with staff code: " + staffCode));

        Optional<TaskAssignment> existing =
                repository.findByTask_DescriptionAndAssignUser_StaffCode(taskDescription, staffCode);

        if (existing.isPresent()) {
            throw new RuntimeException("Assignment already exists");
        }

        return saveAssignment(task, user);
    }

    // =========================
    // GET METHODS
    // =========================
    public List<TaskAssignmentDTOResponse> getAllAssignments() {
        return repository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public Optional<TaskAssignmentDTOResponse> getAssignmentByCode(String code) {
        return repository.findByTaskAssignCode(code).map(this::toResponseDTO);
    }

    public List<TaskAssignmentDTOResponse> getAssignmentsByTaskId(Long taskId) {
        return repository.findByTask_TaskId(taskId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<TaskAssignmentDTOResponse> getAssignmentsByStaffCode(Long staffCode) {
        return repository.findByAssignUser_StaffCode(staffCode).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<TaskAssignmentDTOResponse> getAssignmentsByStatus(TaskStatus status) {
        return repository.findByStatus(status).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // ✅ STAFF UPDATE THEIR OWN ASSIGNMENT STATUS - FIXED
    // =========================
    @Transactional
    public Optional<TaskAssignmentDTOResponse> updateMyAssignmentStatus(Long taskId, Long staffId, TaskStatus newStatus) {
        Optional<TaskAssignment> optionalAssignment =
                repository.findByTaskIdAndStaffId(taskId, staffId);

        if (optionalAssignment.isEmpty()) {
            log.warn("Assignment not found for taskId: {} and staffId: {}", taskId, staffId);
            return Optional.empty();
        }

        TaskAssignment assignment = optionalAssignment.get();
        TaskStatus oldStatus = assignment.getStatus();

        // Validate transition
        validateStatusTransition(oldStatus, newStatus);

        // ✅ ONLY update this specific assignment - DO NOT update other assignments
        assignment.setStatus(newStatus);
        repository.save(assignment);

        log.info("✅ Staff {} updated their assignment {} from {} to {}",
                staffId, assignment.getTaskAssignCode(), oldStatus, newStatus);

        // ✅ Update task status based on ALL assignments
        syncTaskStatusFromAssignments(taskId);

        return Optional.of(toResponseDTO(assignment));
    }

    // =========================
    // ✅ SYNC TASK STATUS BASED ON ALL ASSIGNMENTS
    // =========================
    private void syncTaskStatusFromAssignments(Long taskId) {
        List<TaskAssignment> assignments = repository.findByTask_TaskId(taskId);
        if (assignments.isEmpty()) return;

        Task task = assignments.get(0).getTask();
        TaskStatus currentTaskStatus = task.getTaskStatus();

        // Determine the collective status
        boolean allAssigned = assignments.stream().allMatch(a -> a.getStatus() == TaskStatus.ASSIGNED);
        boolean allInitiated = assignments.stream().allMatch(a -> a.getStatus() == TaskStatus.INITIATED);
        boolean allInProgress = assignments.stream().allMatch(a -> a.getStatus() == TaskStatus.IN_PROGRESS);
        boolean allCompleted = assignments.stream().allMatch(a -> a.getStatus() == TaskStatus.COMPLETED);
        boolean allPendingReview = assignments.stream().allMatch(a -> a.getStatus() == TaskStatus.PENDING_REVIEW);
        boolean allApproved = assignments.stream().allMatch(a -> a.getStatus() == TaskStatus.APPROVED);

        boolean anyRejected = assignments.stream().anyMatch(a -> a.getStatus() == TaskStatus.REJECTED);
        boolean anyInProgress = assignments.stream().anyMatch(a ->
                a.getStatus() == TaskStatus.IN_PROGRESS ||
                        a.getStatus() == TaskStatus.ASSIGNED ||
                        a.getStatus() == TaskStatus.INITIATED);

        boolean anyPendingReview = assignments.stream().anyMatch(a -> a.getStatus() == TaskStatus.PENDING_REVIEW);
        boolean anyCompleted = assignments.stream().anyMatch(a -> a.getStatus() == TaskStatus.COMPLETED);

        TaskStatus newTaskStatus = currentTaskStatus;

        if (allApproved) {
            newTaskStatus = TaskStatus.APPROVED;
        } else if (allPendingReview) {
            newTaskStatus = TaskStatus.PENDING_REVIEW;
        } else if (allCompleted) {
            newTaskStatus = TaskStatus.COMPLETED;
        } else if (allInProgress) {
            newTaskStatus = TaskStatus.IN_PROGRESS;
        } else if (allInitiated) {
            newTaskStatus = TaskStatus.INITIATED;
        } else if (allAssigned) {
            newTaskStatus = TaskStatus.ASSIGNED;
        } else if (anyRejected || anyInProgress) {
            newTaskStatus = TaskStatus.IN_PROGRESS;
        } else if (anyPendingReview) {
            newTaskStatus = TaskStatus.PENDING_REVIEW;
        } else if (anyCompleted) {
            newTaskStatus = TaskStatus.COMPLETED;
        }

        if (newTaskStatus != currentTaskStatus) {
            task.setTaskStatus(newTaskStatus);
            taskRepository.save(task);
            log.info("📋 Task {} status synced from {} to {} based on assignments",
                    taskId, currentTaskStatus, newTaskStatus);
        }
    }

    // =========================
    // STATUS UPDATE (Admin/Manual)
    // =========================
    @Transactional
    public Optional<TaskAssignmentDTOResponse> updateStatus(String code, TaskStatus newStatus) {
        return repository.findByTaskAssignCode(code)
                .map(assignment -> {
                    validateStatusTransition(assignment.getStatus(), newStatus);
                    assignment.setStatus(newStatus);
                    repository.save(assignment);

                    // ✅ Sync task status from all assignments
                    syncTaskStatusFromAssignments(assignment.getTask().getTaskId());

                    return toResponseDTO(assignment);
                });
    }

    // =========================
    // FULL UPDATE (Admin)
    // =========================
    @Transactional
    public Optional<TaskAssignmentDTOResponse> updateAssignment(String code, TaskAssignmentDTO dto) {
        return repository.findByTaskAssignCode(code)
                .map(assignment -> {
                    if (dto.getTaskId() != null) {
                        Task task = taskRepository.findById(dto.getTaskId())
                                .orElseThrow(() -> new RuntimeException("Task not found"));
                        assignment.setTask(task);
                    }

                    if (dto.getStaffId() != null) {
                        User user = userRepository.findById(dto.getStaffId())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                        assignment.setAssignUser(user);
                    }

                    if (dto.getStatus() != null) {
                        TaskStatus newStatus = TaskStatus.valueOf(dto.getStatus());
                        validateStatusTransition(assignment.getStatus(), newStatus);
                        assignment.setStatus(newStatus);
                    }

                    repository.save(assignment);

                    // ✅ Sync task status from all assignments
                    syncTaskStatusFromAssignments(assignment.getTask().getTaskId());

                    return toResponseDTO(assignment);
                });
    }

    // =========================
    // DELETE
    // =========================
    @Transactional
    public void deleteByCode(String code) {
        Optional<TaskAssignment> assignmentOpt = repository.findByTaskAssignCode(code);
        if (assignmentOpt.isPresent()) {
            Long taskId = assignmentOpt.get().getTask().getTaskId();
            repository.deleteByTaskAssignCode(code);

            // ✅ Sync task status after deletion
            syncTaskStatusFromAssignments(taskId);
        }
    }

    // =========================
    // SAVE ASSIGNMENT
    // =========================
    private TaskAssignmentDTOResponse saveAssignment(Task task, User user) {
        String code = generateTaskAssignCode();
        TaskAssignmentId id = new TaskAssignmentId(user.getStaffId(), task.getTaskId());

        TaskAssignment assignment = new TaskAssignment();
        assignment.setId(id);
        assignment.setTask(task);
        assignment.setAssignUser(user);
        assignment.setTaskAssignCode(code);
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setStatus(TaskStatus.ASSIGNED);

        // ✅ Set task status if null
        if (task.getTaskStatus() == null) {
            task.setTaskStatus(TaskStatus.ASSIGNED);
        }
        taskRepository.save(task);

        TaskAssignment saved = repository.save(assignment);

        // ✅ Sync task status based on all assignments
        syncTaskStatusFromAssignments(task.getTaskId());

        return toResponseDTO(saved);
    }

    // =========================
    // CODE GENERATION
    // =========================
    private String generateTaskAssignCode() {
        int year = Year.now().getValue();
        Optional<TaskAssignment> last = repository.findTopByOrderByTaskAssignCodeDesc();
        int seq = 1;

        if (last.isPresent()) {
            String[] parts = last.get().getTaskAssignCode().split("/");
            if (parts.length == 3) {
                try {
                    seq = Integer.parseInt(parts[2]) + 1;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("ASS/%d/%03d", year, seq);
    }

    // =========================
    // DTO MAPPER
    // =========================
    private TaskAssignmentDTOResponse toResponseDTO(TaskAssignment a) {
        Task task = a.getTask();
        User user = a.getAssignUser();

        String departmentName = (user.getDepartment() != null)
                ? user.getDepartment().getDepartmentName()
                : "N/A";

        String branchName = (user.getBranch() != null)
                ? user.getBranch().getBranchName()
                : "N/A";

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

    // =========================
    // VALIDATION
    // =========================
    private void validateStatusTransition(TaskStatus current, TaskStatus next) {
        if (current == null || next == null || current == next) return;

        boolean valid = switch (current) {
            case ASSIGNED -> next == TaskStatus.INITIATED;
            case INITIATED -> next == TaskStatus.IN_PROGRESS;
            case IN_PROGRESS -> next == TaskStatus.COMPLETED;
            case COMPLETED -> next == TaskStatus.PENDING_REVIEW;
            case PENDING_REVIEW -> next == TaskStatus.APPROVED ||
                    next == TaskStatus.REJECTED ||
                    next == TaskStatus.PENDING_APPROVAL;
            case REJECTED -> next == TaskStatus.IN_PROGRESS;
            case PENDING_APPROVAL -> next == TaskStatus.APPROVED || next == TaskStatus.REJECTED;
            default -> false;
        };

        if (!valid) {
            throw new RuntimeException("Invalid status transition from " + current + " to " + next);
        }
    }
}