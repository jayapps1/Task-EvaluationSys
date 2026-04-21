package com.evaluationsys.taskevaluationsys.dtoresponse;

import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StaffAssignmentGroupDTOResponse {

    private Long staffCode;
    private String staffName;
    private String staffDepartment;
    private String staffBranch;
    private List<TaskAssignmentDTOResponse> assignments = new ArrayList<>();

    public StaffAssignmentGroupDTOResponse() {
    }

    public StaffAssignmentGroupDTOResponse(Long staffCode, String staffName,
                                           String staffDepartment, String staffBranch,
                                           List<TaskAssignmentDTOResponse> assignments) {
        this.staffCode = staffCode;
        this.staffName = staffName;
        this.staffDepartment = staffDepartment;
        this.staffBranch = staffBranch;
        this.assignments = (assignments != null) ? assignments : new ArrayList<>();
    }

    // =========================
    // GETTERS
    // =========================
    public Long getStaffCode() {
        return staffCode;
    }

    public String getStaffName() {
        return staffName;
    }

    public String getStaffDepartment() {
        return staffDepartment;
    }

    public String getStaffBranch() {
        return staffBranch;
    }

    public List<TaskAssignmentDTOResponse> getAssignments() {
        return assignments;
    }

    // =========================
    // SETTERS
    // =========================
    public void setStaffCode(Long staffCode) {
        this.staffCode = staffCode;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public void setStaffDepartment(String staffDepartment) {
        this.staffDepartment = staffDepartment;
    }

    public void setStaffBranch(String staffBranch) {
        this.staffBranch = staffBranch;
    }

    public void setAssignments(List<TaskAssignmentDTOResponse> assignments) {
        this.assignments = (assignments != null) ? assignments : new ArrayList<>();
    }

    // =========================
    // HELPER METHODS
    // =========================
    public void addAssignment(TaskAssignmentDTOResponse assignment) {
        if (this.assignments == null) {
            this.assignments = new ArrayList<>();
        }
        this.assignments.add(assignment);
    }

    public int getAssignmentCount() {
        return (assignments == null) ? 0 : assignments.size();
    }

    public int getTaskCount() {
        if (assignments == null) return 0;
        return (int) assignments.stream()
                .map(TaskAssignmentDTOResponse::getTaskId)
                .distinct()
                .count();
    }

    public boolean hasAssignments() {
        return assignments != null && !assignments.isEmpty();
    }

    // =========================
    // ADDITIONAL HELPFUL METHODS
    // =========================

    /**
     * Get count of assignments by status
     */
    public long getCountByStatus(String status) {
        if (assignments == null) return 0;
        return assignments.stream()
                .filter(a -> status.equals(a.getStatus()))
                .count();
    }

    /**
     * Get pending review count for this staff
     */
    public long getPendingReviewCount() {
        return getCountByStatus("PENDING_REVIEW");
    }

    /**
     * Get in progress count for this staff
     */
    public long getInProgressCount() {
        return getCountByStatus("IN_PROGRESS");
    }

    /**
     * Get approved count for this staff
     */
    public long getApprovedCount() {
        return getCountByStatus("APPROVED");
    }

    /**
     * Get assignments filtered by status
     */
    public List<TaskAssignmentDTOResponse> getAssignmentsByStatus(String status) {
        if (assignments == null) return new ArrayList<>();
        return assignments.stream()
                .filter(a -> status.equals(a.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Get pending review assignments for this staff
     */
    public List<TaskAssignmentDTOResponse> getPendingReviewAssignments() {
        return getAssignmentsByStatus("PENDING_REVIEW");
    }

    /**
     * Check if staff has any pending review assignments
     */
    public boolean hasPendingReviews() {
        return getPendingReviewCount() > 0;
    }

    /**
     * Get the earliest deadline among all assignments
     */
    public java.time.LocalDateTime getEarliestDeadline() {
        if (assignments == null || assignments.isEmpty()) return null;
        return assignments.stream()
                .map(a -> a.getTaskDeadline()) // Assuming TaskAssignmentDTOResponse has getTaskDeadline()
                .filter(d -> d != null)
                .min(java.time.LocalDateTime::compareTo)
                .orElse(null);
    }

    /**
     * Get assignments grouped by status
     */
    public Map<String, List<TaskAssignmentDTOResponse>> getAssignmentsGroupedByStatus() {
        if (assignments == null) return Map.of();
        return assignments.stream()
                .collect(Collectors.groupingBy(TaskAssignmentDTOResponse::getStatus));
    }

    /**
     * Get summary statistics for this staff
     */
    public String getSummary() {
        return String.format("%s (%d tasks, %d pending, %d in progress, %d approved)",
                staffName, getTaskCount(), getPendingReviewCount(),
                getInProgressCount(), getApprovedCount());
    }

    /**
     * Get completion rate (percentage of approved tasks)
     */
    public double getCompletionRate() {
        int total = getTaskCount();
        if (total == 0) return 0.0;
        return (getApprovedCount() * 100.0) / total;
    }

    /**
     * Get formatted completion rate
     */
    public String getFormattedCompletionRate() {
        return String.format("%.1f%%", getCompletionRate());
    }

    @Override
    public String toString() {
        return "StaffAssignmentGroupDTOResponse{" +
                "staffCode=" + staffCode +
                ", staffName='" + staffName + '\'' +
                ", staffDepartment='" + staffDepartment + '\'' +
                ", staffBranch='" + staffBranch + '\'' +
                ", assignmentCount=" + getAssignmentCount() +
                ", taskCount=" + getTaskCount() +
                ", pendingReviews=" + getPendingReviewCount() +
                '}';
    }
}