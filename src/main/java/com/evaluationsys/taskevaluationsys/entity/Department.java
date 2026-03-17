package com.evaluationsys.taskevaluationsys.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "department")
public class Department {

    @Id
    @SequenceGenerator(
            name = "department_sequence",
            sequenceName = "department_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "department_sequence"
    )
    @Column(name = "department_id")
    private Long departmentId;
    @Column(unique = true, nullable = false)
    private String departmentCode;

    private String departmentName;

    // =========================
    // RELATIONSHIP WITH BRANCH
    // =========================
    @ManyToOne
    @JoinColumn(
            name = "branch_id",
            foreignKey = @ForeignKey(name = "fk_department_branch")
    )
    private Branch branch;

    // =========================
    // TIMESTAMPS
    // =========================
    @Column(updatable = false)
    private LocalDateTime created_at;

    private LocalDateTime updated_at;

    // =========================
    // CONSTRUCTORS
    // =========================
    public Department() {
    }

    public Department(Long departmentId,String departmentCode, String departmentName, Branch branch,
                      LocalDateTime created_at, LocalDateTime updated_at) {
        this.departmentId = departmentId;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.branch = branch;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }


// =========================
    // GETTERS & SETTERS
    // =========================

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentCode() {return departmentCode;}
    public void setDepartmentCode(String departmentCode) {this.departmentCode = departmentCode;}

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }

    // =========================
    // AUTO TIMESTAMP HANDLING
    // =========================
    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        updated_at = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }

    // =========================
    // DEBUG STRING
    // =========================
    @Override
    public String toString() {
        return "Department{" +
                "departmentId=" + departmentId +
                ", departmentCode='" + departmentCode + '\'' +
                ", departmentName='" + departmentName + '\'' +
                ", branch=" + branch +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
                '}';
    }
}