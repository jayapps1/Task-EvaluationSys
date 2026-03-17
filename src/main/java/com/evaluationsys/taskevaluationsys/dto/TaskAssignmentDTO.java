package com.evaluationsys.taskevaluationsys.dto;

public class TaskAssignmentDTO {
    private Long taskId;
    private Long staffId;

    public TaskAssignmentDTO() {}

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
}