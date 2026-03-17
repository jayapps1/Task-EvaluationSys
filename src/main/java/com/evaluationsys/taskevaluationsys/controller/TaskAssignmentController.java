package com.evaluationsys.taskevaluationsys.controller;

import com.evaluationsys.taskevaluationsys.dto.TaskAssignmentDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.TaskAssignmentDTOResponse;
import com.evaluationsys.taskevaluationsys.service.TaskAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/taskAssignments")
public class TaskAssignmentController {

    private final TaskAssignmentService service;

    public TaskAssignmentController(TaskAssignmentService service) {
        this.service = service;
    }

    // =========================
    // CREATE TASK ASSIGNMENT
    // =========================
    @PostMapping("/create")
    public ResponseEntity<TaskAssignmentDTOResponse> create(@RequestBody TaskAssignmentDTO dto) {
        TaskAssignmentDTOResponse response = service.createAssignment(dto);
        return ResponseEntity.ok(response);
    }

    // =========================
    // GET ALL TASK ASSIGNMENTS
    // =========================
    @GetMapping("/all")
    public ResponseEntity<List<TaskAssignmentDTOResponse>> getAll() {
        return ResponseEntity.ok(service.getAllAssignments());
    }

    // =========================
    // GET TASK ASSIGNMENT BY CODE
    // =========================
    @GetMapping("/by-code")
    public ResponseEntity<TaskAssignmentDTOResponse> getByCode(@RequestParam String taskAssignCode) {
        Optional<TaskAssignmentDTOResponse> assignment = service.getAssignmentByCode(taskAssignCode);
        return assignment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // =========================
    // UPDATE TASK ASSIGNMENT STATUS
    // =========================
    @PatchMapping("/updateStatus")
    public ResponseEntity<TaskAssignmentDTOResponse> updateStatus(
            @RequestParam String taskAssignCode,
            @RequestParam String status
    ) {
        Optional<TaskAssignmentDTOResponse> updated = service.updateStatus(taskAssignCode, status);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // =========================
    // DELETE TASK ASSIGNMENT
    // =========================
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam String taskAssignCode) {
        service.deleteByCode(taskAssignCode);
        return ResponseEntity.noContent().build();
    }
}