package com.evaluationsys.taskevaluationsys.dtoresponse;

import java.time.LocalDateTime;

public class TaskAssignmentDTOResponse {
    private String taskAssignCode;
    private Long taskId;
    private Long staffId;
    private LocalDateTime assignedAt;
    private String status;

    public TaskAssignmentDTOResponse() {}

    public TaskAssignmentDTOResponse(String taskAssignCode, Long taskId, Long staffId,
                                     LocalDateTime assignedAt, String status) {
        this.taskAssignCode = taskAssignCode;
        this.taskId = taskId;
        this.staffId = staffId;
        this.assignedAt = assignedAt;
        this.status = status;
    }

    // Getters & Setters
    public String getTaskAssignCode() { return taskAssignCode; }
    public void setTaskAssignCode(String taskAssignCode) { this.taskAssignCode = taskAssignCode; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}