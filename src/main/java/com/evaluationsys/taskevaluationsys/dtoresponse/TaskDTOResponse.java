package com.evaluationsys.taskevaluationsys.dtoresponse;

import java.time.LocalDateTime;

public class TaskDTOResponse {

    private String taskCode;
    private String description;
    private String supervisorName;
    private String createdSupervisorName;
    private String departmentName;
    private LocalDateTime deadline;
    private String taskStatus;
    private Integer quarter;
    private Integer year;

    public TaskDTOResponse() {}

    // Getters & Setters
    public String getTaskCode() { return taskCode; }
    public void setTaskCode(String taskCode) { this.taskCode = taskCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSupervisorName() { return supervisorName; }
    public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }

    public String getCreatedSupervisorName() { return createdSupervisorName; }
    public void setCreatedSupervisorName(String createdSupervisorName) { this.createdSupervisorName = createdSupervisorName; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }

    public Integer getQuarter() { return quarter; }
    public void setQuarter(Integer quarter) { this.quarter = quarter; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
}