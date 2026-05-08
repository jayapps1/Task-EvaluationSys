package com.evaluationsys.taskevaluationsys.controller;

import com.evaluationsys.taskevaluationsys.dto.TaskDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.TaskDTOResponse;
import com.evaluationsys.taskevaluationsys.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tasks")
@CrossOrigin // ✅ IMPORTANT if frontend is separate (Select2 AJAX)
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // ===============================
    // GET ALL TASKS (PAGINATED - FIXED)
    // ===============================
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {

        // Prevent negative or zero page
        int safePage = Math.max(page, 1);

        Pageable pageable = PageRequest.of(
                safePage - 1,
                size,
                Sort.by(Sort.Direction.DESC, "taskId")
        );

        Page<TaskDTOResponse> taskPage = taskService.getAllTasksPaginated(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("tasks", taskPage.getContent());
        response.put("currentPage", taskPage.getNumber() + 1);
        response.put("totalPages", taskPage.getTotalPages());
        response.put("totalItems", taskPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    // ===============================
    // GET TASK BY ID
    // ===============================
    @GetMapping("/id/{taskId}")
    public ResponseEntity<TaskDTOResponse> getTaskById(@PathVariable Long taskId) {

        return taskService.getTaskById(taskId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ===============================
    // CREATE TASK - WITH VALIDATION ERROR HANDLING
    // ===============================
    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskDTO dto) {
        try {
            TaskDTOResponse created = taskService.createTask(dto);
            return ResponseEntity
                    .created(URI.create("/tasks/id/" + created.getTaskId()))
                    .body(created);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // ===============================
    // UPDATE TASK
    // ===============================
    @PutMapping("/id/{taskId}")
    public ResponseEntity<?> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskDTO dto) {

        return taskService.updateTask(taskId, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ===============================
    // DELETE TASK
    // ===============================
    @DeleteMapping("/id/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {

        boolean deleted = taskService.deleteTask(taskId);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    // ===============================
    // SEARCH TASKS BY CODE
    // ===============================
    @GetMapping("/search")
    public ResponseEntity<List<TaskDTOResponse>> searchTasks(
            @RequestParam(required = false) String q) {

        List<TaskDTOResponse> tasks;

        if (q == null || q.trim().isEmpty()) {
            tasks = taskService.getAllTasks(); // return all tasks
        } else {
            tasks = taskService.searchTasksByCodeOrDescription(q); // search by code or description
        }

        return ResponseEntity.ok(tasks);
    }

    // ===============================
    // HANDLE VALIDATION ERRORS
    // ===============================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> ((FieldError) error).getField(),
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> existing
                ));

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", "Validation failed");
        errorResponse.put("errors", fieldErrors);
        errorResponse.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}