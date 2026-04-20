package com.evaluationsys.taskevaluationsys.dto;

import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public class TaskAssignmentDTO {

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotNull(message = "Staff ID is required")
    private Long staffId;

    private String status;  // Will accept values like "ASSIGNED", "IN_PROGRESS", etc.

    public TaskAssignmentDTO() {}

    public TaskAssignmentDTO(Long taskId, Long staffId, String status) {
        this.taskId = taskId;
        this.staffId = staffId;
        this.status = status;
    }

    // Getters and Setters
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Helper method to convert string status to enum
    public TaskStatus getStatusAsEnum() {
        if (status == null) return null;
        try {
            return TaskStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}