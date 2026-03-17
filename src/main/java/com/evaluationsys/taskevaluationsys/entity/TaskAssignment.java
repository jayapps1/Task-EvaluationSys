package com.evaluationsys.taskevaluationsys.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class TaskAssignment {

    @EmbeddedId
    private TaskAssignmentId id;

    @ManyToOne
    @MapsId("taskId")
    @JoinColumn(
            name = "task_id",
            foreignKey = @ForeignKey(name = "fk_taskassign_task")
    )
    private Task task;

    @ManyToOne
    @MapsId("staffId")
    @JoinColumn(
            name = "staff_id",
            foreignKey = @ForeignKey(name = "fk_taskassign_user")
    )
    private User assignUser;

    @Column(unique = true)
    private String taskAssignCode;

    private LocalDateTime assignedAt;
    private String status;

    public TaskAssignment() {}

    public TaskAssignment(TaskAssignmentId id, Task task, User assignUser,
                          String taskAssignCode, LocalDateTime assignedAt, String status) {
        this.id = id;
        this.task = task;
        this.assignUser = assignUser;
        this.taskAssignCode = taskAssignCode;
        this.assignedAt = assignedAt;
        this.status = status;
    }

    // Getters and Setters
    public TaskAssignmentId getId() { return id; }
    public void setId(TaskAssignmentId id) { this.id = id; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public User getAssignUser() { return assignUser; }
    public void setAssignUser(User assignUser) { this.assignUser = assignUser; }

    public String getTaskAssignCode() { return taskAssignCode; }
    public void setTaskAssignCode(String taskAssignCode) { this.taskAssignCode = taskAssignCode; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "TaskAssignment{" +
                "id=" + id +
                ", taskAssignCode='" + taskAssignCode + '\'' +
                ", task=" + task +
                ", assignUser=" + assignUser +
                ", assignedAt=" + assignedAt +
                ", status='" + status + '\'' +
                '}';
    }
}