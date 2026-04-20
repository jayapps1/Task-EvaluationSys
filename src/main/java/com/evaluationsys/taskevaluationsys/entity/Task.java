package com.evaluationsys.taskevaluationsys.entity;

import com.evaluationsys.taskevaluationsys.entity.enums.Quarter;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "task")
public class Task {

    @Id
    @SequenceGenerator(
            name = "task_sequence",
            sequenceName = "task_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = SEQUENCE, generator = "task_sequence")
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "task_code", unique = true, nullable = false)
    private String taskCode;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supervisor_id", foreignKey = @ForeignKey(name = "fk_task_supervisor"))
    private Supervisor supervisor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_task_created_user"))
    private User createdBy;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    // ============================
    // Use enums instead of String/Integer
    // ============================
    @Enumerated(EnumType.STRING)
    @Column(name = "task_status")
    private TaskStatus taskStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "quarter")
    private Quarter quarter;

    @Column(name = "year")
    private Integer year;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Task() {}

    // ===============================
    // GETTERS & SETTERS
    // ===============================

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getTaskCode() { return taskCode; }
    public void setTaskCode(String taskCode) { this.taskCode = taskCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Supervisor getSupervisor() { return supervisor; }
    public void setSupervisor(Supervisor supervisor) { this.supervisor = supervisor; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public TaskStatus getTaskStatus() { return taskStatus; }
    public void setTaskStatus(TaskStatus taskStatus) { this.taskStatus = taskStatus; }

    public Quarter getQuarter() { return quarter; }
    public void setQuarter(Quarter quarter) { this.quarter = quarter; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ===============================
    // AUTO TIMESTAMPS
    // ===============================

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}