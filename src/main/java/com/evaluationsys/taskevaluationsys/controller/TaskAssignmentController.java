package com.evaluationsys.taskevaluationsys.controller;

import com.evaluationsys.taskevaluationsys.dto.TaskAssignmentDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.TaskAssignmentDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
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
    // CREATE TASK ASSIGNMENT BY IDs
    // =========================
    @PostMapping("/create")
    public ResponseEntity<TaskAssignmentDTOResponse> create(@RequestBody TaskAssignmentDTO dto) {
        TaskAssignmentDTOResponse response = service.createAssignment(dto);
        return ResponseEntity.ok(response);
    }

    // =========================
    // CREATE TASK ASSIGNMENT BY DESCRIPTION + STAFF CODE
    // =========================
    @PostMapping("/createByDescAndCode")
    public ResponseEntity<TaskAssignmentDTOResponse> createByDescAndCode(
            @RequestBody TaskAssignByDescCodeRequest request
    ) {
        TaskAssignmentDTOResponse response = service.createAssignmentByDescAndCode(
                request.getTaskDescription(),
                request.getStaffCode()
        );
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
    // UPDATE TASK ASSIGNMENT STATUS - FIXED
    // =========================
    @PatchMapping("/updateStatus")
    public ResponseEntity<TaskAssignmentDTOResponse> updateStatus(
            @RequestParam String taskAssignCode,
            @RequestParam String status
    ) {
        // Convert String status to TaskStatus enum
        TaskStatus taskStatus;
        try {
            taskStatus = TaskStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        Optional<TaskAssignmentDTOResponse> updated = service.updateStatus(taskAssignCode, taskStatus);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // =========================
    // UPDATE TASK ASSIGNMENT (Full update)
    // =========================
    @PatchMapping("/update")
    public ResponseEntity<TaskAssignmentDTOResponse> updateAssignment(
            @RequestParam String taskAssignCode,
            @RequestBody TaskAssignmentDTO dto
    ) {
        Optional<TaskAssignmentDTOResponse> updated = service.updateAssignment(taskAssignCode, dto);
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

    // =========================
    // REQUEST BODY CLASS FOR createByDescAndCode
    // =========================
    public static class TaskAssignByDescCodeRequest {
        private String taskDescription;
        private Long staffCode;

        public String getTaskDescription() { return taskDescription; }
        public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }

        public Long getStaffCode() { return staffCode; }
        public void setStaffCode(Long staffCode) { this.staffCode = staffCode; }
    }
}