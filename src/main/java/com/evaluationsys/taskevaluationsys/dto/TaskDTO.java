package com.evaluationsys.taskevaluationsys.dto;

import java.time.LocalDateTime;

public class TaskDTO {

    private String description;
    private String supervisorCode;     // Supervisor assigned to this task (by code)
    private String createdByCode;      // Optional: supervisor who created this task (by code)
    private Long departmentId;
    private LocalDateTime deadline;
    private String taskStatus;
    private Integer quarter;
    private Integer year;

    public TaskDTO() {}

    // Getters & Setters
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSupervisorCode() { return supervisorCode; }
    public void setSupervisorCode(String supervisorCode) { this.supervisorCode = supervisorCode; }

    public String getCreatedByCode() { return createdByCode; }
    public void setCreatedByCode(String createdByCode) { this.createdByCode = createdByCode; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }

    public Integer getQuarter() { return quarter; }
    public void setQuarter(Integer quarter) { this.quarter = quarter; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
}