package com.evaluationsys.taskevaluationsys.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
public class Resource {

    @Id
    @SequenceGenerator(
            name = "resource_sequence",
            sequenceName = "resource_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "resource_sequence"
    )
    private Long resource_id;
    private String resourceItem;
    private String description;
    private boolean availabilityStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Resource() {

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

    public Resource(LocalDateTime updatedAt, LocalDateTime createdAt, boolean availabilityStatus, String description, String resourceItem, Long resource_id) {
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
        this.availabilityStatus = availabilityStatus;
        this.description = description;
        this.resourceItem = resourceItem;
        this.resource_id = resource_id;
    }

    public Long getResource_id() {
        return resource_id;
    }

    public void setResource_id(Long resource_id) {
        this.resource_id = resource_id;
    }

    public String getResourceItem() {
        return resourceItem;
    }

    public void setResourceItem(String resourceItem) {
        this.resourceItem = resourceItem;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(boolean availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
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
        return "Resource{" +
                "resource_id=" + resource_id +
                ", resourceItem='" + resourceItem + '\'' +
                ", description='" + description + '\'' +
                ", availabilityStatus=" + availabilityStatus +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
