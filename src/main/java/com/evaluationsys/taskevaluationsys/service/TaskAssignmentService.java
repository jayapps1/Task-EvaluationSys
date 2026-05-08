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
        if (dto == null) {
            throw new IllegalArgumentException("TaskAssignmentDTO cannot be null");
        }

        if (dto.getTaskId() == null || dto.getTaskId() <= 0) {
            throw new IllegalArgumentException("Valid Task ID must be provided (positive number)");
        }

        if (dto.getStaffId() == null || dto.getStaffId() <= 0) {
            throw new IllegalArgumentException("Valid Staff ID must be provided (positive number)");
        }

        TaskAssignmentId id = new TaskAssignmentId(dto.getStaffId(), dto.getTaskId());
        if (repository.existsById(id)) {
            throw new RuntimeException("Assignment already exists for this task and staff member");
        }

        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + dto.getTaskId()));

        validateTaskData(task);

        User user = userRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getStaffId()));

        validateUserData(user);
        validateUserEligibilityForTask(user, task);

        return saveAssignment(task, user);
    }

    // =========================
    // CREATE BY DESCRIPTION + STAFF CODE
    // =========================
    @Transactional
    public TaskAssignmentDTOResponse createAssignmentByDescAndCode(String taskDescription, Long staffCode) {
        if (taskDescription == null || taskDescription.isBlank()) {
            throw new IllegalArgumentException("Task description cannot be null or empty");
        }

        if (staffCode == null || staffCode <= 0) {
            throw new IllegalArgumentException("Valid staff code must be provided (positive number)");
        }

        Task task = taskRepository.findByDescription(taskDescription)
                .orElseThrow(() -> new RuntimeException("Task not found with description: " + taskDescription));

        validateTaskData(task);

        if (task.getDescription() == null || task.getDescription().trim().isEmpty()) {
            throw new RuntimeException("Task has invalid description (empty or null)");
        }

        User user = userRepository.findByStaffCode(staffCode)
                .orElseThrow(() -> new RuntimeException("User not found with staff code: " + staffCode));

        validateUserData(user);

        Optional<TaskAssignment> existing =
                repository.findByTask_DescriptionAndAssignUser_StaffCode(taskDescription, staffCode);

        if (existing.isPresent()) {
            throw new RuntimeException("Assignment already exists for task: " + taskDescription +
                    " and staff code: " + staffCode);
        }

        return saveAssignment(task, user);
    }

    // =========================
    // GET ALL ASSIGNMENTS
    // =========================
    public List<TaskAssignmentDTOResponse> getAllAssignments() {
        List<TaskAssignment> assignments = repository.findAll();

        if (assignments.isEmpty()) {
            log.info("No assignments found in database");
            return new ArrayList<>();
        }

        return assignments.stream()
                .filter(Objects::nonNull)
                .map(this::toResponseDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // =========================
    // GET ASSIGNMENT BY CODE
    // =========================
    public Optional<TaskAssignmentDTOResponse> getAssignmentByCode(String code) {
        if (code == null || code.isBlank()) {
            log.warn("Attempted to get assignment with null or empty code");
            return Optional.empty();
        }

        return repository.findByTaskAssignCode(code)
                .map(this::toResponseDTO);
    }

    // =========================
    // GET ASSIGNMENTS BY TASK ID
    // =========================
    public List<TaskAssignmentDTOResponse> getAssignmentsByTaskId(Long taskId) {
        if (taskId == null || taskId <= 0) {
            log.warn("Attempted to get assignments with invalid taskId: {}", taskId);
            return new ArrayList<>();
        }

        return repository.findByTask_TaskId(taskId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // GET ASSIGNMENTS BY STAFF CODE
    // =========================
    public List<TaskAssignmentDTOResponse> getAssignmentsByStaffCode(Long staffCode) {
        if (staffCode == null || staffCode <= 0) {
            log.warn("Attempted to get assignments with invalid staffCode: {}", staffCode);
            return new ArrayList<>();
        }

        return repository.findByAssignUser_StaffCode(staffCode).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // GET ASSIGNMENTS BY STATUS
    // =========================
    public List<TaskAssignmentDTOResponse> getAssignmentsByStatus(TaskStatus status) {
        if (status == null) {
            log.warn("Attempted to get assignments with null status");
            return new ArrayList<>();
        }

        return repository.findByStatus(status).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // ✅ UPDATE STATUS (This is the missing method!)
    // =========================
    @Transactional
    public Optional<TaskAssignmentDTOResponse> updateStatus(String code, TaskStatus newStatus) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Assignment code cannot be null or empty");
        }

        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }

        return repository.findByTaskAssignCode(code)
                .map(assignment -> {
                    TaskStatus oldStatus = assignment.getStatus();

                    // Validate status transition
                    validateStatusTransition(oldStatus, newStatus);

                    // Update the status
                    assignment.setStatus(newStatus);
                    repository.save(assignment);

                    log.info("✅ Assignment {} status updated from {} to {}",
                            code, oldStatus, newStatus);

                    // Sync task status from all assignments
                    syncTaskStatusFromAssignments(assignment.getTask().getTaskId());

                    return toResponseDTO(assignment);
                });
    }

    // =========================
    // ✅ UPDATE MY ASSIGNMENT STATUS (For staff self-updates)
    // =========================
    @Transactional
    public Optional<TaskAssignmentDTOResponse> updateMyAssignmentStatus(Long taskId, Long staffId, TaskStatus newStatus) {
        if (taskId == null || taskId <= 0) {
            throw new IllegalArgumentException("Valid Task ID must be provided");
        }

        if (staffId == null || staffId <= 0) {
            throw new IllegalArgumentException("Valid Staff ID must be provided");
        }

        if (newStatus == null) {
            throw new IllegalArgumentException("New status must be provided");
        }

        Optional<TaskAssignment> optionalAssignment =
                repository.findByTaskIdAndStaffId(taskId, staffId);

        if (optionalAssignment.isEmpty()) {
            log.warn("Assignment not found for taskId: {} and staffId: {}", taskId, staffId);
            return Optional.empty();
        }

        TaskAssignment assignment = optionalAssignment.get();
        TaskStatus oldStatus = assignment.getStatus();

        // Validate status transition
        validateStatusTransition(oldStatus, newStatus);

        // Update the assignment status
        assignment.setStatus(newStatus);
        repository.save(assignment);

        log.info("✅ Staff {} updated their assignment {} from {} to {}",
                staffId, assignment.getTaskAssignCode(), oldStatus, newStatus);

        // Update task status based on ALL assignments
        syncTaskStatusFromAssignments(taskId);

        return Optional.of(toResponseDTO(assignment));
    }

    // =========================
    // ✅ UPDATE ASSIGNMENT (Full update)
    // =========================
    @Transactional
    public Optional<TaskAssignmentDTOResponse> updateAssignment(String code, TaskAssignmentDTO dto) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Assignment code cannot be null or empty");
        }

        if (dto == null) {
            throw new IllegalArgumentException("TaskAssignmentDTO cannot be null");
        }

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
                    log.info("✅ Assignment {} updated", code);

                    // Sync task status from all assignments
                    syncTaskStatusFromAssignments(assignment.getTask().getTaskId());

                    return toResponseDTO(assignment);
                });
    }

    // =========================
    // ✅ DELETE BY CODE
    // =========================
    @Transactional
    public void deleteByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Assignment code cannot be null or empty");
        }

        Optional<TaskAssignment> assignmentOpt = repository.findByTaskAssignCode(code);
        if (assignmentOpt.isPresent()) {
            Long taskId = assignmentOpt.get().getTask().getTaskId();
            repository.deleteByTaskAssignCode(code);
            log.info("Deleted assignment with code: {}", code);

            // Sync task status after deletion
            syncTaskStatusFromAssignments(taskId);
        } else {
            log.warn("Attempted to delete non-existent assignment with code: {}", code);
        }
    }

    // =========================
    // SYNC TASK STATUS FROM ALL ASSIGNMENTS
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
    // SAVE ASSIGNMENT
    // =========================
    private TaskAssignmentDTOResponse saveAssignment(Task task, User user) {
        if (task == null) {
            throw new IllegalArgumentException("Cannot save assignment: Task is null");
        }

        if (user == null) {
            throw new IllegalArgumentException("Cannot save assignment: User is null");
        }

        if (task.getTaskId() == null) {
            throw new IllegalArgumentException("Cannot save assignment: Task has no ID");
        }

        if (user.getStaffId() == null) {
            throw new IllegalArgumentException("Cannot save assignment: User has no staff ID");
        }

        TaskAssignmentId id = new TaskAssignmentId(user.getStaffId(), task.getTaskId());
        if (repository.existsById(id)) {
            throw new RuntimeException("Assignment already exists before save");
        }

        String code = generateTaskAssignCode();

        if (code == null || code.isBlank()) {
            throw new RuntimeException("Failed to generate valid assignment code");
        }

        TaskAssignment assignment = new TaskAssignment();
        assignment.setId(id);
        assignment.setTask(task);
        assignment.setAssignUser(user);
        assignment.setTaskAssignCode(code);
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setStatus(TaskStatus.ASSIGNED);

        if (task.getTaskStatus() == null) {
            task.setTaskStatus(TaskStatus.ASSIGNED);
        }

        if (task.getTaskStatus() == TaskStatus.COMPLETED ||
                task.getTaskStatus() == TaskStatus.APPROVED) {
            throw new RuntimeException("Cannot assign to task that is already " + task.getTaskStatus());
        }

        taskRepository.save(task);

        try {
            TaskAssignment saved = repository.save(assignment);
            log.info("✅ Successfully saved assignment with code: {} for task: {} and user: {}",
                    code, task.getTaskId(), user.getStaffId());

            syncTaskStatusFromAssignments(task.getTaskId());

            return toResponseDTO(saved);
        } catch (Exception e) {
            log.error("Failed to save assignment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save assignment: " + e.getMessage(), e);
        }
    }

    // =========================
    // CODE GENERATION
    // =========================
    private String generateTaskAssignCode() {
        int year = Year.now().getValue();
        Optional<TaskAssignment> last = repository.findTopByOrderByTaskAssignCodeDesc();
        int seq = 1;

        if (last.isPresent() && last.get().getTaskAssignCode() != null) {
            String[] parts = last.get().getTaskAssignCode().split("/");
            if (parts.length == 3) {
                try {
                    seq = Integer.parseInt(parts[2]) + 1;
                    if (seq > 999) {
                        throw new RuntimeException("Assignment sequence exceeded maximum (999) for year " + year);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse sequence from code: {}", last.get().getTaskAssignCode());
                    seq = 1;
                }
            }
        }

        String generatedCode = String.format("ASS/%d/%03d", year, seq);
        log.debug("Generated assignment code: {}", generatedCode);
        return generatedCode;
    }

    // =========================
    // DTO MAPPER
    // =========================
    private TaskAssignmentDTOResponse toResponseDTO(TaskAssignment a) {
        if (a == null) {
            log.warn("Attempted to convert null TaskAssignment to DTO");
            return null;
        }

        Task task = a.getTask();
        User user = a.getAssignUser();

        if (task == null) {
            log.error("TaskAssignment {} has null task", a.getTaskAssignCode());
            throw new RuntimeException("Assignment has invalid task reference");
        }

        if (user == null) {
            log.error("TaskAssignment {} has null user", a.getTaskAssignCode());
            throw new RuntimeException("Assignment has invalid user reference");
        }

        String departmentName = (user.getDepartment() != null && user.getDepartment().getDepartmentName() != null)
                ? user.getDepartment().getDepartmentName()
                : "No Department";

        String branchName = "No Branch";
        if (user.getBranch() != null) {
            branchName = (user.getBranch().getBranchName() != null)
                    ? user.getBranch().getBranchName()
                    : "Unnamed Branch";
        }

        String firstName = (user.getFirstName() != null) ? user.getFirstName() : "";
        String otherName = (user.getOtherName() != null) ? user.getOtherName() : "";
        String taskDescription = (task.getDescription() != null) ? task.getDescription() : "No Description";

        return new TaskAssignmentDTOResponse(
                a.getTaskAssignCode() != null ? a.getTaskAssignCode() : "NO_CODE",
                task.getTaskId(),
                taskDescription,
                task.getDeadline(),
                user.getStaffId(),
                firstName,
                otherName,
                departmentName,
                branchName,
                a.getAssignedAt() != null ? a.getAssignedAt() : LocalDateTime.now(),
                a.getStatus() != null ? a.getStatus().name() : TaskStatus.ASSIGNED.name()
        );
    }

    // =========================
    // VALIDATION METHODS
    // =========================
    private void validateTaskData(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        if (task.getTaskId() == null || task.getTaskId() <= 0) {
            throw new IllegalArgumentException("Task has invalid ID");
        }

        if (task.getDescription() == null || task.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Task description is empty or null. Cannot assign without valid description.");
        }

        if (task.getDeadline() == null) {
            throw new IllegalArgumentException("Task has no deadline set. Please set deadline before assigning.");
        }

        if (task.getDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot assign task with deadline in the past: " + task.getDeadline());
        }

        log.debug("Task validation passed for task ID: {}", task.getTaskId());
    }

    private void validateUserData(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (user.getStaffId() == null || user.getStaffId() <= 0) {
            throw new IllegalArgumentException("User has invalid staff ID");
        }

        if (user.getStaffCode() == null || user.getStaffCode() <= 0) {
            throw new IllegalArgumentException("User has invalid staff code");
        }

        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("User has no first name. Cannot assign task.");
        }

        if (user.getDepartment() == null) {
            log.warn("User {} has no department assigned", user.getStaffCode());
        }

        log.debug("User validation passed for staff ID: {}", user.getStaffId());
    }

    private void validateUserEligibilityForTask(User user, Task task) {
        if (task.getTaskStatus() == TaskStatus.COMPLETED ||
                task.getTaskStatus() == TaskStatus.APPROVED) {
            throw new RuntimeException("Cannot assign completed or approved task to user");
        }

        long activeAssignments = repository.findByAssignUser_StaffCode(user.getStaffCode())
                .stream()
                .filter(a -> a.getStatus() != TaskStatus.COMPLETED &&
                        a.getStatus() != TaskStatus.APPROVED &&
                        a.getStatus() != TaskStatus.REJECTED)
                .count();

        if (activeAssignments >= 5) {
            throw new RuntimeException("User already has " + activeAssignments +
                    " active assignments. Maximum limit is 5.");
        }

        log.debug("User eligibility validation passed for staff {} on task {}",
                user.getStaffCode(), task.getTaskId());
    }

    // =========================
    // VALIDATE STATUS TRANSITION
    // =========================
    private void validateStatusTransition(TaskStatus current, TaskStatus next) {
        if (current == null || next == null) {
            throw new IllegalArgumentException("Current and next status cannot be null");
        }

        if (current == next) return;

        boolean valid = switch (current) {
            case ASSIGNED -> next == TaskStatus.INITIATED;
            case INITIATED -> next == TaskStatus.IN_PROGRESS;
            case IN_PROGRESS -> next == TaskStatus.COMPLETED;
            case COMPLETED -> next == TaskStatus.PENDING_REVIEW || next == TaskStatus.REJECTED;
            case PENDING_REVIEW -> next == TaskStatus.APPROVED || next == TaskStatus.REJECTED;
            case REJECTED -> next == TaskStatus.IN_PROGRESS;
            case PENDING_APPROVAL -> next == TaskStatus.APPROVED || next == TaskStatus.REJECTED;
            case APPROVED -> false;
        };

        if (!valid) {
            throw new RuntimeException("Invalid status transition from " + current + " to " + next);
        }
    }
}