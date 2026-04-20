package com.evaluationsys.taskevaluationsys.dtoresponse;

import com.evaluationsys.taskevaluationsys.entity.enums.Quarter;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;

import java.time.LocalDateTime;

public class TaskDTOResponse {

    // ===============================
    // TASK INFO
    // ===============================
    private Long taskId;
    private String taskCode;
    private String description;

    // ===============================
    // SUPERVISOR INFO
    // ===============================
    private String supervisorCode;
    private String supervisorName;

    // ===============================
    // CREATED BY (USER)
    // ===============================
    private Long createdByStaffCode;
    private String createdByName;

    // ===============================
    // TASK METADATA
    // ===============================
    private LocalDateTime deadline;
    private TaskStatus taskStatus;  // enum
    private Quarter quarter;        // enum
    private Integer year;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===============================
    // CONSTRUCTORS
    // ===============================
    public TaskDTOResponse() {}

    public TaskDTOResponse(
            Long taskId,
            String taskCode,
            String description,
            String supervisorCode,
            String supervisorName,
            Long createdByStaffCode,
            String createdByName,
            LocalDateTime deadline,
            TaskStatus taskStatus,
            Quarter quarter,
            Integer year,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.taskId = taskId;
        this.taskCode = taskCode;
        this.description = description;
        this.supervisorCode = supervisorCode;
        this.supervisorName = supervisorName;
        this.createdByStaffCode = createdByStaffCode;
        this.createdByName = createdByName;
        this.deadline = deadline;
        this.taskStatus = taskStatus;
        this.quarter = quarter;
        this.year = year;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ===============================
    // GETTERS & SETTERS
    // ===============================
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getTaskCode() { return taskCode; }
    public void setTaskCode(String taskCode) { this.taskCode = taskCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSupervisorCode() { return supervisorCode; }
    public void setSupervisorCode(String supervisorCode) { this.supervisorCode = supervisorCode; }

    public String getSupervisorName() { return supervisorName; }
    public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }

    public Long getCreatedByStaffCode() { return createdByStaffCode; }
    public void setCreatedByStaffCode(Long createdByStaffCode) { this.createdByStaffCode = createdByStaffCode; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public TaskStatus getTaskStatus() { return taskStatus; }
    public void setTaskStatus(TaskStatus taskStatus) { this.taskStatus = taskStatus; }

    public Quarter getQuarter() { return quarter; }
    public void setQuarter(Quarter quarter) { this.quarter = quarter; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ===============================
    // HELPER METHODS (OPTIONAL)
    // ===============================
    public String getTaskStatusAsString() {
        return taskStatus != null ? taskStatus.name() : "N/A";
    }

    public String getQuarterAsString() {
        return quarter != null ? quarter.name() : "N/A";
    }
}