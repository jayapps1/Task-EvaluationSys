package com.evaluationsys.taskevaluationsys.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
public class TaskReplacement {

    @Id
    @SequenceGenerator(
            name = "replacement_sequence",
            sequenceName = "replacement_sequence",
            allocationSize = 1

    )
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "replacement_sequence"
    )
    private  Long replacement_id;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "old_task_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_replacement_old_task")
    )
    private Task oldtask;

    @ManyToOne
    @JoinColumn(
            name =  "new_task_id",

            foreignKey = @ForeignKey(name = "fk_replacement_new_task")
    )
    private Task newtask;

    @ManyToOne
    @JoinColumn(
            name = "supervisor_id",
            foreignKey = @ForeignKey(name = "fk_TaskReplace_supervisor")
    )
    private Supervisor supervisor;
    private String replacementReason;
    private LocalDateTime replacementDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TaskReplacement() {

    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public TaskReplacement(Long replacement_id, Task oldtask, Task newtask, Supervisor supervisor, String replacementReason, LocalDateTime replacementDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.replacement_id = replacement_id;
        this.oldtask = oldtask;
        this.newtask = newtask;
        this.supervisor = supervisor;
        this.replacementReason = replacementReason;
        this.replacementDate = replacementDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getReplacement_id() {
        return replacement_id;
    }

    public void setReplacement_id(Long replacement_id) {
        this.replacement_id = replacement_id;
    }

    public Task getOldtask() {
        return oldtask;
    }

    public void setOldtask(Task oldtask) {
        this.oldtask = oldtask;
    }

    public Task getNewtask() {
        return newtask;
    }

    public void setNewtask(Task newtask) {
        this.newtask = newtask;
    }

    public Supervisor getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    public String getReplacementReason() {
        return replacementReason;
    }

    public void setReplacementReason(String replacementReason) {
        this.replacementReason = replacementReason;
    }

    public LocalDateTime getReplacementDate() {
        return replacementDate;
    }

    public void setReplacementDate(LocalDateTime replacementDate) {
        this.replacementDate = replacementDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "TaskReplacement{" +
                "replacement_id=" + replacement_id +
                ", oldtask=" + oldtask +
                ", newtask=" + newtask +
                ", supervisor=" + supervisor +
                ", replacementReason='" + replacementReason + '\'' +
                ", replacementDate=" + replacementDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
