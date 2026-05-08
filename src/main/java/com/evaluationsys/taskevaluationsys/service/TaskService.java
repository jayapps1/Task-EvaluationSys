package com.evaluationsys.taskevaluationsys.service;

import com.evaluationsys.taskevaluationsys.dto.TaskDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.TaskDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.Task;
import com.evaluationsys.taskevaluationsys.entity.TaskAssignment;
import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.entity.enums.Quarter;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import com.evaluationsys.taskevaluationsys.repository.SupervisorRepository;
import com.evaluationsys.taskevaluationsys.repository.TaskAssignmentRepository;
import com.evaluationsys.taskevaluationsys.repository.TaskRepository;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final SupervisorRepository supervisorRepository;
    private final UserRepository userRepository;
    private final TaskAssignmentRepository assignmentRepository;

    public TaskService(TaskRepository taskRepository,
                       SupervisorRepository supervisorRepository,
                       UserRepository userRepository,
                       TaskAssignmentRepository assignmentRepository) {
        this.taskRepository = taskRepository;
        this.supervisorRepository = supervisorRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    // ===============================
    // Helper method to get current quarter based on date
    // ===============================
    private Quarter getCurrentQuarter(LocalDateTime date) {
        int month = date.getMonthValue();
        if (month >= 1 && month <= 3) return Quarter.Q1;
        if (month >= 4 && month <= 6) return Quarter.Q2;
        if (month >= 7 && month <= 9) return Quarter.Q3;
        return Quarter.Q4;
    }

    // ===============================
    // Helper method to get quarter start date
    // ===============================
    private LocalDateTime getQuarterStartDate(Quarter quarter, int year) {
        return switch (quarter) {
            case Q1 -> LocalDateTime.of(year, 1, 1, 0, 0, 0);
            case Q2 -> LocalDateTime.of(year, 4, 1, 0, 0, 0);
            case Q3 -> LocalDateTime.of(year, 7, 1, 0, 0, 0);
            case Q4 -> LocalDateTime.of(year, 10, 1, 0, 0, 0);
        };
    }

    // ===============================
    // Helper method to get quarter end date
    // ===============================
    private LocalDateTime getQuarterEndDate(Quarter quarter, int year) {
        return switch (quarter) {
            case Q1 -> LocalDateTime.of(year, 3, 31, 23, 59, 59);
            case Q2 -> LocalDateTime.of(year, 6, 30, 23, 59, 59);
            case Q3 -> LocalDateTime.of(year, 9, 30, 23, 59, 59);
            case Q4 -> LocalDateTime.of(year, 12, 31, 23, 59, 59);
        };
    }

    // ===============================
    // Helper method to get quarter date range string
    // ===============================
    private String getQuarterDateRange(Quarter quarter, int year) {
        return switch (quarter) {
            case Q1 -> String.format("Q1 (Jan 1 - Mar 31, %d)", year);
            case Q2 -> String.format("Q2 (Apr 1 - Jun 30, %d)", year);
            case Q3 -> String.format("Q3 (Jul 1 - Sep 30, %d)", year);
            case Q4 -> String.format("Q4 (Oct 1 - Dec 31, %d)", year);
        };
    }

    // ===============================
    // STRICT VALIDATION: Only current quarter, deadline within quarter
    // ===============================
    private void validateStrictQuarterAndDeadline(LocalDateTime currentDate, TaskDTO dto) {
        Quarter currentQuarter = getCurrentQuarter(currentDate);
        Quarter taskQuarter = dto.getQuarter();
        Integer taskYear = dto.getYear();
        LocalDateTime deadline = dto.getDeadline();
        int currentYear = currentDate.getYear();

        // Rule 1: Can only create tasks for the current year
        if (taskYear == null || !taskYear.equals(currentYear)) {
            throw new IllegalArgumentException(
                    String.format("❌ Tasks can only be created for the current year (%d). You selected year %d.",
                            currentYear, taskYear)
            );
        }

        // Rule 2: Can only create tasks for the current quarter
        if (taskQuarter != currentQuarter) {
            throw new IllegalArgumentException(
                    String.format("❌ Tasks can only be created for the current quarter (%s - %s). You selected %s.",
                            currentQuarter, getQuarterDateRange(currentQuarter, currentYear), taskQuarter)
            );
        }

        // Rule 3: Deadline must be within the current quarter
        LocalDateTime quarterStart = getQuarterStartDate(currentQuarter, currentYear);
        LocalDateTime quarterEnd = getQuarterEndDate(currentQuarter, currentYear);

        if (deadline == null) {
            throw new IllegalArgumentException("❌ Task deadline is required");
        }

        if (deadline.isBefore(quarterStart) || deadline.isAfter(quarterEnd)) {
            throw new IllegalArgumentException(
                    String.format("❌ Deadline must be within the current quarter (%s): %s to %s. Your deadline: %s",
                            currentQuarter,
                            quarterStart.toLocalDate().toString(),
                            quarterEnd.toLocalDate().toString(),
                            deadline.toLocalDate().toString())
            );
        }

        // Rule 4: Deadline cannot be in the past
        if (deadline.isBefore(currentDate)) {
            throw new IllegalArgumentException("❌ Deadline cannot be in the past");
        }
    }

    // ===============================
    // PAGINATED TASKS
    // ===============================
    public Page<TaskDTOResponse> getAllTasksPaginated(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    // ===============================
    // GET TASK BY ID
    // ===============================
    public Optional<TaskDTOResponse> getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .map(this::convertToResponse);
    }

    // ===============================
    // GET ALL TASKS
    // ===============================
    public List<TaskDTOResponse> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ===============================
    // CREATE TASK WITH STRICT VALIDATION
    // ===============================
    @Transactional
    public TaskDTOResponse createTask(TaskDTO dto) {
        // Auto-set year to current year if not provided
        LocalDateTime currentDate = LocalDateTime.now();
        int currentYear = currentDate.getYear();
        Quarter currentQuarter = getCurrentQuarter(currentDate);

        if (dto.getYear() == null) {
            dto.setYear(currentYear);
        }

        // Auto-set quarter to current quarter if not provided or force it
        if (dto.getQuarter() == null) {
            dto.setQuarter(currentQuarter);
        }

        // REQUIRED FIELD VALIDATIONS
        validateRequiredFields(dto);

        // ✅ STRICT QUARTER AND DEADLINE VALIDATION
        validateStrictQuarterAndDeadline(currentDate, dto);

        Task task = new Task();

        // Map DTO → Entity (only after validation passes)
        mapDtoToEntity(dto, task);

        // Set Task Code using the year from DTO
        int year = dto.getYear();
        long count = taskRepository.countByYear(year) + 1;
        task.setTaskCode(String.format("TSK/%02d/%03d", year % 100, count));

        // Default Status
        if (task.getTaskStatus() == null) {
            task.setTaskStatus(TaskStatus.ASSIGNED);
        }

        Task savedTask = taskRepository.save(task);
        return convertToResponse(savedTask);
    }

    // ===============================
    // VALIDATE REQUIRED FIELDS
    // ===============================
    private void validateRequiredFields(TaskDTO dto) {
        List<String> errors = new java.util.ArrayList<>();

        if (dto == null) {
            throw new IllegalArgumentException("Task data cannot be null");
        }

        // Validate Description (required)
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            errors.add("Task description is required and cannot be empty");
        }

        // Validate Deadline (required) - basic check, strict check in validateStrictQuarterAndDeadline
        if (dto.getDeadline() == null) {
            errors.add("Task deadline is required");
        }

        // Validate Quarter (required)
        if (dto.getQuarter() == null) {
            errors.add("Task quarter is required");
        }

        // Validate Supervisor (required)
        if (dto.getSupervisorId() == null) {
            errors.add("Supervisor ID is required");
        }

        // Validate Created By (required)
        if (dto.getCreatedByCode() == null) {
            errors.add("Created by staff code is required");
        }

        // Validate Year (required)
        if (dto.getYear() == null) {
            errors.add("Year is required");
        }

        if (!errors.isEmpty()) {
            String errorMessage = String.join("; ", errors);
            throw new IllegalArgumentException("Validation failed: " + errorMessage);
        }
    }

    // ===============================
    // UPDATE TASK WITH VALIDATION
    // ===============================
    @Transactional
    public Optional<TaskDTOResponse> updateTask(Long taskId, TaskDTO dto) {
        return taskRepository.findById(taskId)
                .map(task -> {
                    TaskStatus oldStatus = task.getTaskStatus();
                    LocalDateTime currentDate = LocalDateTime.now();

                    // Validate update-specific fields
                    if (dto.getDescription() != null && dto.getDescription().trim().isEmpty()) {
                        throw new IllegalArgumentException("Description cannot be empty");
                    }

                    // For updates, validate deadline is within current quarter if being updated
                    if (dto.getDeadline() != null) {
                        Quarter currentQuarter = getCurrentQuarter(currentDate);
                        int currentYear = currentDate.getYear();
                        LocalDateTime quarterStart = getQuarterStartDate(currentQuarter, currentYear);
                        LocalDateTime quarterEnd = getQuarterEndDate(currentQuarter, currentYear);

                        if (dto.getDeadline().isBefore(quarterStart) || dto.getDeadline().isAfter(quarterEnd)) {
                            throw new IllegalArgumentException(
                                    String.format("Deadline must be within the current quarter (%s): %s to %s",
                                            currentQuarter, quarterStart.toLocalDate(), quarterEnd.toLocalDate())
                            );
                        }

                        if (dto.getDeadline().isBefore(currentDate)) {
                            throw new IllegalArgumentException("Deadline cannot be in the past");
                        }
                    }

                    // Update only non-null fields
                    if (dto.getDescription() != null) task.setDescription(dto.getDescription().trim());
                    if (dto.getDeadline() != null) task.setDeadline(dto.getDeadline());
                    if (dto.getTaskStatus() != null) task.setTaskStatus(dto.getTaskStatus());

                    // Handle relations
                    if (dto.getSupervisorId() != null) {
                        supervisorRepository.findById(dto.getSupervisorId())
                                .ifPresentOrElse(
                                        task::setSupervisor,
                                        () -> {
                                            throw new RuntimeException("Supervisor not found with id: " + dto.getSupervisorId());
                                        }
                                );
                    }

                    if (dto.getCreatedByCode() != null) {
                        userRepository.findByStaffCode(dto.getCreatedByCode())
                                .ifPresentOrElse(
                                        task::setCreatedBy,
                                        () -> {
                                            throw new RuntimeException("User not found with staff code: " + dto.getCreatedByCode());
                                        }
                                );
                    }

                    Task savedTask = taskRepository.save(task);

                    // SYNC: If status changed, update all assignments
                    if (dto.getTaskStatus() != null && !dto.getTaskStatus().equals(oldStatus)) {
                        syncAssignmentsWithTaskStatus(taskId, dto.getTaskStatus());
                    }

                    return convertToResponse(savedTask);
                });
    }

    // ===============================
    // DELETE TASK
    // ===============================
    @Transactional
    public boolean deleteTask(Long taskId) {
        return taskRepository.findById(taskId)
                .map(task -> {
                    assignmentRepository.deleteByTask_TaskId(taskId);
                    taskRepository.delete(task);
                    return true;
                })
                .orElse(false);
    }

    // ===============================
    // SEARCH TASKS
    // ===============================
    public List<TaskDTOResponse> searchTasksByCodeOrDescription(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllTasks();
        }
        return taskRepository
                .findByTaskCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ===============================
    // WORKFLOW METHODS
    // ===============================
    @Transactional
    public boolean staffAcceptTask(Long taskId, Long staffCode) {
        Task task = getTaskOrThrow(taskId);
        validateStaffAssignment(task, staffCode);

        if (task.getTaskStatus() != TaskStatus.ASSIGNED) {
            throw new IllegalStateException("Task cannot be accepted. Current status: " + task.getTaskStatus());
        }

        task.setTaskStatus(TaskStatus.INITIATED);
        taskRepository.save(task);
        syncAssignmentsWithTaskStatus(taskId, TaskStatus.INITIATED);

        return true;
    }

    @Transactional
    public boolean staffStartProgress(Long taskId, Long staffCode) {
        Task task = getTaskOrThrow(taskId);
        validateStaffAssignment(task, staffCode);

        if (task.getTaskStatus() != TaskStatus.INITIATED) {
            throw new IllegalStateException("Task cannot be started. Current status: " + task.getTaskStatus());
        }

        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task);
        syncAssignmentsWithTaskStatus(taskId, TaskStatus.IN_PROGRESS);

        return true;
    }

    @Transactional
    public boolean staffMarkComplete(Long taskId, Long staffCode) {
        Task task = getTaskOrThrow(taskId);
        validateStaffAssignment(task, staffCode);

        if (task.getTaskStatus() != TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Task cannot be completed. Current status: " + task.getTaskStatus());
        }

        task.setTaskStatus(TaskStatus.COMPLETED);
        taskRepository.save(task);
        syncAssignmentsWithTaskStatus(taskId, TaskStatus.COMPLETED);

        return true;
    }

    @Transactional
    public boolean staffSubmitForReview(Long taskId, Long staffCode) {
        Task task = getTaskOrThrow(taskId);
        validateStaffAssignment(task, staffCode);

        if (task.getTaskStatus() != TaskStatus.COMPLETED) {
            throw new IllegalStateException("Task cannot be submitted for review. Current status: " + task.getTaskStatus());
        }

        task.setTaskStatus(TaskStatus.PENDING_REVIEW);
        taskRepository.save(task);
        syncAssignmentsWithTaskStatus(taskId, TaskStatus.PENDING_REVIEW);

        return true;
    }

    @Transactional
    public boolean supervisorApprove(Long taskId, String supervisorCode) {
        Task task = getTaskOrThrow(taskId);

        if (task.getTaskStatus() != TaskStatus.COMPLETED && task.getTaskStatus() != TaskStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Task cannot be approved. Current status: " + task.getTaskStatus());
        }

        if (task.getSupervisor() == null ||
                !task.getSupervisor().getSupervisorCode().equals(supervisorCode)) {
            throw new RuntimeException("Supervisor mismatch or not authorized to approve this task");
        }

        task.setTaskStatus(TaskStatus.APPROVED);
        taskRepository.save(task);
        syncAssignmentsWithTaskStatus(taskId, TaskStatus.APPROVED);

        return true;
    }

    @Transactional
    public boolean supervisorReject(Long taskId, String supervisorCode, String reason) {
        Task task = getTaskOrThrow(taskId);

        if (task.getTaskStatus() != TaskStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Task cannot be rejected. Current status: " + task.getTaskStatus());
        }

        if (task.getSupervisor() == null ||
                !task.getSupervisor().getSupervisorCode().equals(supervisorCode)) {
            throw new RuntimeException("Supervisor mismatch or not authorized to reject this task");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }

        task.setTaskStatus(TaskStatus.REJECTED);
        taskRepository.save(task);
        syncAssignmentsWithTaskStatus(taskId, TaskStatus.REJECTED);

        return true;
    }

    // ===============================
    // VALIDATE STAFF ASSIGNMENT
    // ===============================
    private void validateStaffAssignment(Task task, Long staffCode) {
        boolean isAssigned = assignmentRepository.existsByTask_TaskIdAndAssignUser_StaffCode(
                task.getTaskId(), staffCode);

        if (!isAssigned) {
            throw new RuntimeException("Staff with code " + staffCode +
                    " is not assigned to task " + task.getTaskId());
        }
    }

    // ===============================
    // SYNC ASSIGNMENTS WITH TASK STATUS
    // ===============================
    private void syncAssignmentsWithTaskStatus(Long taskId, TaskStatus newStatus) {
        List<TaskAssignment> assignments = assignmentRepository.findByTask_TaskId(taskId);
        for (TaskAssignment assignment : assignments) {
            assignment.setStatus(newStatus);
            assignmentRepository.save(assignment);
        }
    }

    // ===============================
    // HELPERS
    // ===============================
    private Task getTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
    }

    private void mapDtoToEntity(TaskDTO dto, Task task) {
        task.setDescription(dto.getDescription().trim());
        task.setDeadline(dto.getDeadline());
        task.setQuarter(dto.getQuarter());
        task.setTaskStatus(dto.getTaskStatus() != null ? dto.getTaskStatus() : TaskStatus.ASSIGNED);
        task.setYear(dto.getYear());
        handleRelations(dto, task);
    }

    private void handleRelations(TaskDTO dto, Task task) {
        if (dto.getSupervisorId() == null) {
            throw new IllegalArgumentException("Supervisor ID is required");
        }

        supervisorRepository.findById(dto.getSupervisorId())
                .ifPresentOrElse(
                        task::setSupervisor,
                        () -> {
                            throw new RuntimeException("Supervisor not found with id: " + dto.getSupervisorId());
                        }
                );

        if (dto.getCreatedByCode() == null) {
            throw new IllegalArgumentException("Created by staff code is required");
        }

        userRepository.findByStaffCode(dto.getCreatedByCode())
                .ifPresentOrElse(
                        task::setCreatedBy,
                        () -> {
                            throw new RuntimeException("User not found with staff code: " + dto.getCreatedByCode());
                        }
                );
    }

    // ===============================
    // ENTITY → DTO RESPONSE
    // ===============================
    private TaskDTOResponse convertToResponse(Task task) {
        TaskDTOResponse resp = new TaskDTOResponse();

        resp.setTaskId(task.getTaskId());
        resp.setTaskCode(task.getTaskCode());
        resp.setDescription(task.getDescription());
        resp.setTaskStatus(task.getTaskStatus());
        resp.setDeadline(task.getDeadline());
        resp.setQuarter(task.getQuarter());
        resp.setYear(task.getYear());
        resp.setCreatedAt(task.getCreatedAt());
        resp.setUpdatedAt(task.getUpdatedAt());

        if (task.getSupervisor() != null) {
            resp.setSupervisorCode(String.valueOf(task.getSupervisor().getSupervisorId()));
            String fullName = getFullName(task.getSupervisor().getUser());
            resp.setSupervisorName(fullName.isEmpty() ? "N/A" : fullName);
        } else {
            resp.setSupervisorCode("N/A");
            resp.setSupervisorName("N/A");
        }

        if (task.getCreatedBy() != null) {
            resp.setCreatedByStaffCode(task.getCreatedBy().getStaffCode());
            String fullName = getFullName(task.getCreatedBy());
            resp.setCreatedByName(fullName.isEmpty() ? "N/A" : fullName);
        } else {
            resp.setCreatedByStaffCode(null);
            resp.setCreatedByName("N/A");
        }

        return resp;
    }

    private String getFullName(User user) {
        if (user == null) return "";
        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String other = user.getOtherName() != null ? user.getOtherName() : "";
        return (first + " " + other).trim();
    }
}