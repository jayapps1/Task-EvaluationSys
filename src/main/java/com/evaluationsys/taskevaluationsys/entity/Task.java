package com.evaluationsys.taskevaluationsys.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
public class Task {

    @Id
    @SequenceGenerator(
            name = "task_sequence",
            sequenceName = "task_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = SEQUENCE, generator = "task_sequence")
    private Long task_id;

    @Column(unique = true)
    private String taskCode;

    private String description;

    @ManyToOne
    @JoinColumn(name = "supervisor_id", foreignKey = @ForeignKey(name = "fk_task_supervisor"))
    private Supervisor supervisor;

    @ManyToOne
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_task_created_supervisor"))
    private Supervisor createdSupervisor;

    @ManyToOne
    @JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_task_department"))
    private Department department;

    private LocalDateTime deadline;
    private String taskStatus;
    private Integer quarter;
    private Integer year;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public Task() {}

    // Getters and setters
    public Long getTask_id() { return task_id; }
    public void setTask_id(Long task_id) { this.task_id = task_id; }

    public String getTaskCode() { return taskCode; }
    public void setTaskCode(String taskCode) { this.taskCode = taskCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Supervisor getSupervisor() { return supervisor; }
    public void setSupervisor(Supervisor supervisor) { this.supervisor = supervisor; }

    public Supervisor getCreatedSupervisor() { return createdSupervisor; }
    public void setCreatedSupervisor(Supervisor createdSupervisor) { this.createdSupervisor = createdSupervisor; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }

    public Integer getQuarter() { return quarter; }
    public void setQuarter(Integer quarter) { this.quarter = quarter; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public LocalDateTime getCreated_at() { return created_at; }
    public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }

    public LocalDateTime getUpdated_at() { return updated_at; }
    public void setUpdated_at(LocalDateTime updated_at) { this.updated_at = updated_at; }

    @PrePersist
    public void prePersist() {
        this.created_at = LocalDateTime.now();
        this.updated_at = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updated_at = LocalDateTime.now();
    }
}