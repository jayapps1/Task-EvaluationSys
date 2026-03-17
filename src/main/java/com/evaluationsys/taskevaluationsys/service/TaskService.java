package com.evaluationsys.taskevaluationsys.service;

import com.evaluationsys.taskevaluationsys.dto.TaskDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.TaskDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.Task;
import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.repository.DepartmentRepository;
import com.evaluationsys.taskevaluationsys.repository.SupervisorRepository;
import com.evaluationsys.taskevaluationsys.repository.TaskRepository;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final SupervisorRepository supervisorRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository,
                       SupervisorRepository supervisorRepository,
                       DepartmentRepository departmentRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.supervisorRepository = supervisorRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    // GET ALL TASKS
    public List<TaskDTOResponse> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // GET TASK BY CODE
    public Optional<TaskDTOResponse> getTaskByCode(String taskCode) {
        return taskRepository.findByTaskCode(taskCode)
                .map(this::convertToResponse);
    }

    // CREATE TASK
    public TaskDTOResponse createTask(TaskDTO dto) {

        Task task = new Task();

        mapDtoToEntity(dto, task);

        int year = LocalDateTime.now().getYear();
        task.setYear(year);

        Optional<Task> lastTask = taskRepository.findTopByYear(year);
        long sequence = lastTask.map(t -> t.getTask_id() + 1).orElse(1L);

        String yearShort = String.format("%02d", year % 100);
        task.setTaskCode(String.format("TSK/%s/%03d", yearShort, sequence));

        // created supervisor using code
        if (dto.getCreatedByCode() != null) {
            supervisorRepository.findBySupervisorCode(dto.getCreatedByCode())
                    .ifPresent(task::setCreatedSupervisor);
        }

        Task saved = taskRepository.save(task);

        return convertToResponse(saved);
    }

    // UPDATE TASK
    public Optional<TaskDTOResponse> updateTask(String taskCode, TaskDTO dto) {

        return taskRepository.findByTaskCode(taskCode)
                .map(task -> {

                    mapDtoToEntity(dto, task);

                    Task updated = taskRepository.save(task);

                    return convertToResponse(updated);
                });
    }

    // DELETE TASK
    public boolean deleteTask(String taskCode) {

        return taskRepository.findByTaskCode(taskCode)
                .map(task -> {
                    taskRepository.delete(task);
                    return true;
                })
                .orElse(false);
    }

    // DTO → ENTITY
    private void mapDtoToEntity(TaskDTO dto, Task task) {

        task.setDescription(dto.getDescription());
        task.setTaskStatus(dto.getTaskStatus());
        task.setDeadline(dto.getDeadline());
        task.setQuarter(dto.getQuarter());
        task.setYear(dto.getYear());

        // supervisor assignment using supervisorCode
        if (dto.getSupervisorCode() != null) {
            supervisorRepository.findBySupervisorCode(dto.getSupervisorCode())
                    .ifPresent(task::setSupervisor);
        }

        if (dto.getDepartmentId() != null) {
            departmentRepository.findById(dto.getDepartmentId())
                    .ifPresent(task::setDepartment);
        }
    }

    // ENTITY → RESPONSE DTO
    private TaskDTOResponse convertToResponse(Task task) {

        TaskDTOResponse resp = new TaskDTOResponse();

        resp.setTaskCode(task.getTaskCode());
        resp.setDescription(task.getDescription());
        resp.setTaskStatus(task.getTaskStatus());
        resp.setDeadline(task.getDeadline());
        resp.setQuarter(task.getQuarter());
        resp.setYear(task.getYear());

        // Assigned supervisor name
        if (task.getSupervisor() != null && task.getSupervisor().getUser() != null) {

            User user = task.getSupervisor().getUser();

            resp.setSupervisorName(
                    user.getFirstName() + " " + user.getOtherName()
            );
        }

        // Created supervisor name
        if (task.getCreatedSupervisor() != null && task.getCreatedSupervisor().getUser() != null) {

            User user = task.getCreatedSupervisor().getUser();

            resp.setCreatedSupervisorName(
                    user.getFirstName() + " " + user.getOtherName()
            );
        }

        // Department
        if (task.getDepartment() != null) {
            resp.setDepartmentName(task.getDepartment().getDepartmentName());
        }

        return resp;
    }
}