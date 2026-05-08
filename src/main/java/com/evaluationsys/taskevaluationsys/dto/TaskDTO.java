package com.evaluationsys.taskevaluationsys.dto;

import com.evaluationsys.taskevaluationsys.entity.enums.Quarter;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class TaskDTO {

    @NotNull(message = "Task description is required")
    @NotBlank(message = "Task description cannot be blank")
    @Size(min = 3, max = 500, message = "Task description must be between 3 and 500 characters")
    private String description;

    @NotNull(message = "Supervisor ID is required")
    @Positive(message = "Supervisor ID must be a positive number")
    private Long supervisorId;

    @NotNull(message = "Created by staff code is required")
    @Positive(message = "Created by staff code must be a positive number")
    private Long createdByCode;

    @NotNull(message = "Task deadline is required")
    @Future(message = "Task deadline must be in the future")
    private LocalDateTime deadline;

    private TaskStatus taskStatus;

    @NotNull(message = "Quarter is required")
    private Quarter quarter;

    @NotNull(message = "Year is required")
    @Min(value = 2024, message = "Year must be 2024 or later")
    @Max(value = 2030, message = "Year must be 2030 or earlier")
    private Integer year;

    // Constructors
    public TaskDTO() {}

    public TaskDTO(String description,
                   Long supervisorId,
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

    public Long getSupervisorId() { return supervisorId; }
    public void setSupervisorId(Long supervisorId) { this.supervisorId = supervisorId; }

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