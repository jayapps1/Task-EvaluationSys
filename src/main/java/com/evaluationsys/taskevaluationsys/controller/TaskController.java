package com.evaluationsys.taskevaluationsys.controller;

import com.evaluationsys.taskevaluationsys.dto.TaskDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.TaskDTOResponse;
import com.evaluationsys.taskevaluationsys.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskDTOResponse>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{taskCode}")
    public ResponseEntity<TaskDTOResponse> getTaskByCode(@PathVariable String taskCode) {
        return taskService.getTaskByCode(taskCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TaskDTOResponse> createTask(@RequestBody TaskDTO dto) {

        TaskDTOResponse created = taskService.createTask(dto);

        return ResponseEntity
                .created(URI.create("/tasks/" + created.getTaskCode()))
                .body(created);
    }

    @PutMapping("/{taskCode}")
    public ResponseEntity<TaskDTOResponse> updateTask(@PathVariable String taskCode,
                                                      @RequestBody TaskDTO dto) {
        return taskService.updateTask(taskCode, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{taskCode}")
    public ResponseEntity<Void> deleteTask(@PathVariable String taskCode) {
        return taskService.deleteTask(taskCode) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private Long getLoggedInStaffId() {
        // Replace this with actual authentication logic
        // Example: fetch from SecurityContextHolder
        return 1L;
    }
}