package com.evaluationsys.taskevaluationsys.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
public class Branch {

    @Id
    @SequenceGenerator(
            name = "branch_sequence",
            sequenceName = "branch_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "branch_sequence"
    )
    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(unique = true, nullable = false)
    private String branchCode;

    @Column(nullable = false, unique = true)
    private String branchName;

    private String location;


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Branch() {
    }


    public Branch(Long branchId, String branchCode, String branchName, String location, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.branchId = branchId;
        this.branchCode = branchCode;
        this.branchName = branchName;
        this.location = location;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    @Override
    public String toString() {
        return "Branch{" +
                "branchId=" + branchId +
                "branchCode='" + branchCode + '\'' +
                ", branchName='" + branchName + '\'' +
                ", location='" + location + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}