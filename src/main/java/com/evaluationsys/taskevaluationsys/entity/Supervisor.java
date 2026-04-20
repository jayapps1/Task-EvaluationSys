package com.evaluationsys.taskevaluationsys.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
public class Supervisor {

    @Id
    @SequenceGenerator(
            name = "supervisor_sequence",
            sequenceName = "supervisor_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "supervisor_sequence"
    )
    private Long supervisorId;

    @Column(unique = true, nullable = false)
    private String supervisorCode;

    @ManyToOne
    @JoinColumn(
            name = "department_id",
            foreignKey = @ForeignKey(name = "fk_supervisor_department")
    )
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "staff_id",
            foreignKey = @ForeignKey(name = "fk_supervisor_user")
    )
    private User user;


    @ManyToOne
    @JoinColumn(
            name = "branch_id",
            foreignKey = @ForeignKey(name = "fk_supervisor_branch")
    )
    private Branch branch;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Supervisor() {}

    public Supervisor(String supervisorCode, Department department, User user, Branch branch) {
        this.supervisorCode = supervisorCode;
        this.department = department;
        this.user = user;
        this.branch = branch;
    }

    public Long getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(Long supervisorId) {
        this.supervisorId = supervisorId;
    }

    public String getSupervisorCode() {
        return supervisorCode;
    }

    public void setSupervisorCode(String supervisorCode) {
        this.supervisorCode = supervisorCode;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}