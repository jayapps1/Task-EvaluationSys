package com.evaluationsys.taskevaluationsys.entity.enums;

public enum TaskStatus {

    ASSIGNED("Assigned"),
    INITIATED("Initiated"),
    IN_PROGRESS("In Progress"),
    PENDING_REVIEW("Pending Review"),      // Staff done, waiting for Supervisor
    PENDING_APPROVAL("Pending Approval"),  // Supervisor approved, waiting for Admin
    REJECTED("Rejected"),                  // Supervisor/Admin rejected
    APPROVED("Approved"),                  // Final completed state
    COMPLETED("Completed");                // Alias for APPROVED (for reporting)

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isFinalState() {
        return this == APPROVED || this == COMPLETED;
    }
}