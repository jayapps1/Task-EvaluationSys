package com.evaluationsys.taskevaluationsys.service;

import com.evaluationsys.taskevaluationsys.dto.TaskAssignmentDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.TaskAssignmentDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.Task;
import com.evaluationsys.taskevaluationsys.entity.TaskAssignment;
import com.evaluationsys.taskevaluationsys.entity.TaskAssignmentId;
import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.repository.TaskAssignmentRepository;
import com.evaluationsys.taskevaluationsys.repository.TaskRepository;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskAssignmentService {

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

    // Create assignment
    public TaskAssignmentDTOResponse createAssignment(TaskAssignmentDTO dto) {
        // Validate DTO
        if (dto.getTaskId() == null || dto.getStaffId() == null) {
            throw new IllegalArgumentException("Task ID and Staff ID must be provided");
        }

        // Fetch Task and User
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        User user = userRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate unique assignment code
        String code = generateTaskAssignCode();

        // Create composite key
        TaskAssignmentId id = new TaskAssignmentId(user.getStaffId(), task.getTask_id());

        // Create TaskAssignment entity
        TaskAssignment assignment = new TaskAssignment();
        assignment.setId(id);          // must set composite ID
        assignment.setTask(task);
        assignment.setAssignUser(user);
        assignment.setTaskAssignCode(code);
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setStatus("ASSIGNED");

        // Save to DB
        TaskAssignment saved = repository.save(assignment);

        // Return response DTO
        return toResponseDTO(saved);
    }

    // Get all assignments
    public List<TaskAssignmentDTOResponse> getAllAssignments() {
        return repository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get by code
    public Optional<TaskAssignmentDTOResponse> getAssignmentByCode(String code) {
        return repository.findByTaskAssignCode(code)
                .map(this::toResponseDTO);
    }

    // Update status by code
    public Optional<TaskAssignmentDTOResponse> updateStatus(String code, String status) {
        return repository.findByTaskAssignCode(code)
                .map(assignment -> {
                    assignment.setStatus(status);
                    repository.save(assignment);
                    return toResponseDTO(assignment);
                });
    }

    //  Delete by code
    public void deleteByCode(String code) {
        repository.deleteByTaskAssignCode(code);
    }

    // --- Helpers ---
    private String generateTaskAssignCode() {
        int year = Year.now().getValue();
        Optional<TaskAssignment> last = repository.findTopByOrderByTaskAssignCodeDesc();
        int sequence = 1;

        if (last.isPresent()) {
            String[] parts = last.get().getTaskAssignCode().split("/");
            int lastYear = Integer.parseInt(parts[1]);
            int lastSeq = Integer.parseInt(parts[2]);
            sequence = (lastYear == year) ? lastSeq + 1 : 1;
        }

        return String.format("ASS/%d/%03d", year, sequence);
    }

    private TaskAssignmentDTOResponse toResponseDTO(TaskAssignment assignment) {
        return new TaskAssignmentDTOResponse(
                assignment.getTaskAssignCode(),
                assignment.getTask().getTask_id(),
                assignment.getAssignUser().getStaffId(),
                assignment.getAssignedAt(),
                assignment.getStatus()
        );
    }
}