package com.evaluationsys.taskevaluationsys.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
public class Evaluation {

    @Id
    @SequenceGenerator(
            name = "evaluation_sequence",
            sequenceName = "evaluation_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "evaluation_sequence"
    )
    private Long evaluationId;

    @Column(unique = true)
    private String evaluationCode;

    @ManyToOne
    @JoinColumn(
            name = "task_id",
            foreignKey = @ForeignKey(name = "fk_evaluation_task")
    )
    private Task task;

    @ManyToOne
    @JoinColumn(
            name = "supervisor_id",
            foreignKey = @ForeignKey(name = "fk_evaluation_supervisor")
    )
    private Supervisor supervisor;

    private Double score;
    private String remarks;
    private LocalDateTime evaluationDate;
    private Integer year;
    private Integer quarter;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Evaluation() {}

    public Evaluation(Long evaluationId, String evaluationCode, Task task, Supervisor supervisor, Double score, String remarks, LocalDateTime evaluationDate, Integer year, Integer quarter, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.evaluationId = evaluationId;
        this.evaluationCode = evaluationCode;
        this.task = task;
        this.supervisor = supervisor;
        this.score = score;
        this.remarks = remarks;
        this.evaluationDate = evaluationDate;
        this.year = year;
        this.quarter = quarter;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(Long evaluationId) {
        this.evaluationId = evaluationId;
    }

    public String getEvaluationCode() {
        return evaluationCode;
    }

    public void setEvaluationCode(String evaluationCode) {
        this.evaluationCode = evaluationCode;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Supervisor getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(LocalDateTime evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
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
        return "Evaluation{" +
                "evaluationId=" + evaluationId +
                ", evaluationCode='" + evaluationCode + '\'' +
                ", task=" + task +
                ", supervisor=" + supervisor +
                ", score=" + score +
                ", remarks='" + remarks + '\'' +
                ", evaluationDate=" + evaluationDate +
                ", year=" + year +
                ", quarter=" + quarter +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}