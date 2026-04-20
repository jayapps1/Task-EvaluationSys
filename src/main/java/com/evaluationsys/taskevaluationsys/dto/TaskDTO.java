package com.evaluationsys.taskevaluationsys.dto;

import com.evaluationsys.taskevaluationsys.entity.enums.Quarter;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import java.time.LocalDateTime;

public class TaskDTO {

    private String description;

    // Changed from String to Long - now matches database
    private Long supervisorId;  // This will be the ID from supervisor table

    private Long createdByCode;  // This is the staff_code from user table (also an ID)

    private LocalDateTime deadline;
    private TaskStatus taskStatus;
    private Quarter quarter;
    private Integer year;

    // Constructors
    public TaskDTO() {}

    public TaskDTO(String description,
                   Long supervisorId,  // Changed
                   Long createdByCode,
                   LocalDateTime deadline,
                   TaskStatus taskStatus,
                   Quarter quarter,
                   Integer year) {
        this.description = description;
        this.supervisorId = supervisorId;
        this.createdByCode = createdByCode;
        this.deadline = deadline;
        this.taskStatus = taskStatus;
        this.quarter = quarter;
        this.year = year;
    }

    // Getters & Setters
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getSupervisorId() { return supervisorId; }  // Changed
    public void setSupervisorId(Long supervisorId) { this.supervisorId = supervisorId; }  // Changed

    public Long getCreatedByCode() { return createdByCode; }
    public void setCreatedByCode(Long createdByCode) { this.createdByCode = createdByCode; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public TaskStatus getTaskStatus() { return taskStatus; }
    public void setTaskStatus(TaskStatus taskStatus) { this.taskStatus = taskStatus; }

    public Quarter getQuarter() { return quarter; }
    public void setQuarter(Quarter quarter) { this.quarter = quarter; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
}