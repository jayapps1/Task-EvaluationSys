package com.evaluationsys.taskevaluationsys.service;

import com.evaluationsys.taskevaluationsys.dto.TaskDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.TaskDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.Task;
import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.entity.enums.Quarter;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import com.evaluationsys.taskevaluationsys.repository.SupervisorRepository;
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

    public TaskService(TaskRepository taskRepository,
                       SupervisorRepository supervisorRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.supervisorRepository = supervisorRepository;
        this.userRepository = userRepository;
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
    // CREATE TASK
    // ===============================
    @Transactional
    public TaskDTOResponse createTask(TaskDTO dto) {
        Task task = new Task();

        // Map DTO → Entity
        mapDtoToEntity(dto, task);

        // Set Year & Task Code
        int year = LocalDateTime.now().getYear();
        task.setYear(year);
        long count = taskRepository.countByYear(year) + 1;
        task.setTaskCode(String.format("TSK/%02d/%03d", year % 100, count));

        // Default Status
        if (task.getTaskStatus() == null) {
            task.setTaskStatus(TaskStatus.ASSIGNED);
        }

        return convertToResponse(taskRepository.save(task));
    }

    // ===============================
    // UPDATE TASK
    // ===============================
    @Transactional
    public Optional<TaskDTOResponse> updateTask(Long taskId, TaskDTO dto) {
        return taskRepository.findById(taskId)
                .map(task -> {
                    if (dto.getDescription() != null) task.setDescription(dto.getDescription());
                    if (dto.getDeadline() != null) task.setDeadline(dto.getDeadline());
                    if (dto.getQuarter() != null) task.setQuarter(dto.getQuarter());
                    if (dto.getYear() != null) task.setYear(dto.getYear());
                    if (dto.getTaskStatus() != null) task.setTaskStatus(dto.getTaskStatus());

                    handleRelations(dto, task);

                    return convertToResponse(taskRepository.save(task));
                });
    }

    // ===============================
    // DELETE TASK
    // ===============================
    @Transactional
    public boolean deleteTask(Long taskId) {
        return taskRepository.findById(taskId)
                .map(task -> {
                    taskRepository.delete(task);
                    return true;
                })
                .orElse(false);
    }

    // ===============================
    // SEARCH TASKS
    // ===============================
    public List<TaskDTOResponse> searchTasksByCodeOrDescription(String query) {
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
        if (task.getTaskStatus() != TaskStatus.ASSIGNED) return false;
        task.setTaskStatus(TaskStatus.INITIATED);
        return true;
    }

    @Transactional
    public boolean staffStartProgress(Long taskId, Long staffCode) {
        Task task = getTaskOrThrow(taskId);
        if (task.getTaskStatus() != TaskStatus.INITIATED) return false;
        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        return true;
    }

    @Transactional
    public boolean staffMarkComplete(Long taskId, Long staffCode) {
        Task task = getTaskOrThrow(taskId);
        if (task.getTaskStatus() != TaskStatus.IN_PROGRESS) return false;
        task.setTaskStatus(TaskStatus.COMPLETED);
        return true;
    }

    @Transactional
    public boolean supervisorApprove(Long taskId, String supervisorCode) {
        Task task = getTaskOrThrow(taskId);
        if (task.getTaskStatus() != TaskStatus.COMPLETED) return false;

        if (task.getSupervisor() == null ||
                !task.getSupervisor().getSupervisorCode().equals(supervisorCode)) {
            throw new RuntimeException("Supervisor mismatch");
        }

        task.setTaskStatus(TaskStatus.APPROVED);
        return true;
    }

    // ===============================
    // HELPERS
    // ===============================
    private Task getTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    private void mapDtoToEntity(TaskDTO dto, Task task) {
        if (dto.getDescription() != null) task.setDescription(dto.getDescription());
        if (dto.getDeadline() != null) task.setDeadline(dto.getDeadline());
        if (dto.getQuarter() != null) task.setQuarter(dto.getQuarter());
        if (dto.getTaskStatus() != null) task.setTaskStatus(dto.getTaskStatus());
        if (dto.getYear() != null) task.setYear(dto.getYear());

        handleRelations(dto, task);
    }

    // FIXED: Changed to use supervisorId instead of supervisorCode
    private void handleRelations(TaskDTO dto, Task task) {
        // Supervisor - Now using ID (Long) instead of code (String)
        if (dto.getSupervisorId() != null) {
            supervisorRepository.findById(dto.getSupervisorId())
                    .ifPresentOrElse(
                            task::setSupervisor,
                            () -> {
                                throw new RuntimeException("Supervisor not found with id: " + dto.getSupervisorId());
                            }
                    );
        } else {
            task.setSupervisor(null);
        }

        // Created By - Using staff code (Long)
        if (dto.getCreatedByCode() != null) {
            userRepository.findByStaffCode(dto.getCreatedByCode())
                    .ifPresentOrElse(
                            task::setCreatedBy,
                            () -> {
                                throw new RuntimeException("User not found with staff code: " + dto.getCreatedByCode());
                            }
                    );
        } else {
            task.setCreatedBy(null);
        }
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

        // Supervisor - Return ID as String in supervisorCode field
        if (task.getSupervisor() != null) {
            // Store the ID as String for frontend to use when sending back
            resp.setSupervisorCode(String.valueOf(task.getSupervisor().getSupervisorId()));
            String fullName = getFullName(task.getSupervisor().getUser());
            resp.setSupervisorName(fullName.isEmpty() ? "N/A" : fullName);
        } else {
            resp.setSupervisorCode("N/A");
            resp.setSupervisorName("N/A");
        }

        // Created By - Return staff code ID
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

    // Helper to safely build full name
    private String getFullName(User user) {
        if (user == null) return "";
        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String other = user.getOtherName() != null ? user.getOtherName() : "";
        return (first + " " + other).trim();
    }
}