package com.evaluationsys.taskevaluationsys.service.supervisor;

public class SupervisorStatistics {
    private int totalTasks;
    private int initiatedTasks;      // ASSIGNED + INITIATED
    private int inProgressTasks;     // IN_PROGRESS
    private int pendingReviewTasks;  // PENDING_REVIEW (needs supervisor review)
    private int pendingApprovalTasks; // PENDING_APPROVAL (needs admin approval)
    private int approvedTasks;       // APPROVED (final)
    private double approvalRate;

    public SupervisorStatistics() {}

    // Getters
    public int getTotalTasks() { return totalTasks; }
    public int getInitiatedTasks() { return initiatedTasks; }
    public int getInProgressTasks() { return inProgressTasks; }
    public int getPendingReviewTasks() { return pendingReviewTasks; }
    public int getPendingApprovalTasks() { return pendingApprovalTasks; }
    public int getApprovedTasks() { return approvedTasks; }
    public double getApprovalRate() { return approvalRate; }

    // Setters
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
    public void setInitiatedTasks(int initiatedTasks) { this.initiatedTasks = initiatedTasks; }
    public void setInProgressTasks(int inProgressTasks) { this.inProgressTasks = inProgressTasks; }
    public void setPendingReviewTasks(int pendingReviewTasks) { this.pendingReviewTasks = pendingReviewTasks; }
    public void setPendingApprovalTasks(int pendingApprovalTasks) { this.pendingApprovalTasks = pendingApprovalTasks; }
    public void setApprovedTasks(int approvedTasks) { this.approvedTasks = approvedTasks; }
    public void setApprovalRate(double approvalRate) { this.approvalRate = approvalRate; }

    public String getApprovalRatePercentage() {
        return String.format("%.1f%%", approvalRate);
    }
}