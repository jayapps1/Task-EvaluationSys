package com.evaluationsys.taskevaluationsys.service.staff;

public class StaffTaskStatistics {
    private int totalTasks;
    private int pendingTasks;        // ASSIGNED + INITIATED
    private int inProgressTasks;     // IN_PROGRESS
    private int pendingReviewTasks;  // PENDING_REVIEW (waiting for supervisor)
    private int completedTasks;      // APPROVED + COMPLETED
    private double completionRate;

    public StaffTaskStatistics() {}

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(int pendingTasks) { this.pendingTasks = pendingTasks; }

    public int getInProgressTasks() { return inProgressTasks; }
    public void setInProgressTasks(int inProgressTasks) { this.inProgressTasks = inProgressTasks; }

    public int getPendingReviewTasks() { return pendingReviewTasks; }
    public void setPendingReviewTasks(int pendingReviewTasks) { this.pendingReviewTasks = pendingReviewTasks; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
}