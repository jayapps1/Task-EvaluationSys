package com.evaluationsys.taskevaluationsys.service.admin;

public class AdminStatistics {
    private long totalStaff;
    private long totalSupervisors;
    private long totalBranches;
    private long totalDepartments;
    private int totalTasks;
    private int pendingTasks;        // ASSIGNED
    private int inProgressTasks;     // INITIATED + IN_PROGRESS
    private int pendingReviewTasks;  // PENDING_REVIEW
    private int pendingApprovalTasks; // PENDING_APPROVAL
    private int approvedTasks;       // APPROVED
    private int rejectedTasks;       // REJECTED
    private int completedTasks;      // COMPLETED
    // Getters and Setters
    public long getTotalStaff() { return totalStaff; }
    public void setTotalStaff(long totalStaff) { this.totalStaff = totalStaff; }

    public long getTotalSupervisors() { return totalSupervisors; }
    public void setTotalSupervisors(long totalSupervisors) { this.totalSupervisors = totalSupervisors; }

    public long getTotalBranches() { return totalBranches; }
    public void setTotalBranches(long totalBranches) { this.totalBranches = totalBranches; }

    public long getTotalDepartments() { return totalDepartments; }
    public void setTotalDepartments(long totalDepartments) { this.totalDepartments = totalDepartments; }

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(int pendingTasks) { this.pendingTasks = pendingTasks; }

    public int getInProgressTasks() { return inProgressTasks; }
    public void setInProgressTasks(int inProgressTasks) { this.inProgressTasks = inProgressTasks; }

    public int getPendingReviewTasks() { return pendingReviewTasks; }
    public void setPendingReviewTasks(int pendingReviewTasks) { this.pendingReviewTasks = pendingReviewTasks; }

    public int getPendingApprovalTasks() { return pendingApprovalTasks; }
    public void setPendingApprovalTasks(int pendingApprovalTasks) { this.pendingApprovalTasks = pendingApprovalTasks; }

    public int getApprovedTasks() { return approvedTasks; }
    public void setApprovedTasks(int approvedTasks) { this.approvedTasks = approvedTasks; }

    public int getRejectedTasks() { return rejectedTasks; }
    public void setRejectedTasks(int rejectedTasks) { this.rejectedTasks = rejectedTasks; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
}