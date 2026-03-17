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

    @ManyToOne
    @JoinColumn(
            name = "staff_id",
            foreignKey = @ForeignKey(name = "fk_supervisor_user")
    )
    private User user;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Supervisor() {}

    public Supervisor(String supervisorCode, Department department, User user) {
        this.supervisorCode = supervisorCode;
        this.department = department;
        this.user = user;
    }

    public Long getSupervisorId() {
        return supervisorId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
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