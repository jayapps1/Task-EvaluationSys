package com.evaluationsys.taskevaluationsys.dtoresponse;

import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;

import java.time.LocalDateTime;

public class TaskAssignmentDTOResponse {

    private String taskAssignCode;
    private Long taskId;
    private String taskDescription;
    private LocalDateTime taskDeadline;  // ✅ ADDED THIS FIELD

    private Long staffId;
    private String staffFirstName;
    private String staffOtherName;

    // ✅ MATCH FRONTEND NAMING (IMPORTANT)
    private String staffBranch;
    private String staffDepartment;

    private LocalDateTime assignedAt;

    private String status;
    private String statusDisplayName;
    private String statusBadgeClass;

    public TaskAssignmentDTOResponse() {}

    // ✅ UPDATED CONSTRUCTOR WITH taskDeadline
    public TaskAssignmentDTOResponse(
            String taskAssignCode,
            Long taskId,
            String taskDescription,
            LocalDateTime taskDeadline,     // ✅ ADDED
            Long staffId,
            String staffFirstName,
            String staffOtherName,
            String staffDepartment,
            String staffBranch,
            LocalDateTime assignedAt,
            String status) {

        this.taskAssignCode = taskAssignCode;
        this.taskId = taskId;
        this.taskDescription = taskDescription;
        this.taskDeadline = taskDeadline;   // ✅ ADDED
        this.staffId = staffId;
        this.staffFirstName = staffFirstName;
        this.staffOtherName = staffOtherName;

        // ✅ IMPORTANT: order matches service
        this.staffDepartment = staffDepartment;
        this.staffBranch = staffBranch;

        this.assignedAt = assignedAt;
        setStatus(status); // ensures displayName + badge are set
    }

    // =========================
    // STATUS HELPERS
    // =========================

    private String getDisplayNameFromStatus(String status) {
        if (status == null) return "N/A";
        try {
            return TaskStatus.valueOf(status).getDisplayName();
        } catch (Exception e) {
            return status;
        }
    }

    private String getBadgeClassFromStatus(String status) {
        if (status == null) return "badge bg-secondary";

        return switch (status) {
            case "ASSIGNED" -> "badge bg-secondary";
            case "INITIATED" -> "badge bg-info";
            case "IN_PROGRESS" -> "badge bg-primary";
            case "COMPLETED" -> "badge bg-success";
            case "PENDING_REVIEW" -> "badge bg-warning";
            case "APPROVED" -> "badge bg-success";
            case "REJECTED" -> "badge bg-danger";
            default -> "badge bg-secondary";
        };
    }

    public boolean isFinalState() {
        if (status == null) return false;
        try {
            return TaskStatus.valueOf(status).isFinalState();
        } catch (Exception e) {
            return false;
        }
    }

    public String[] getAllowedNextStatuses() {
        if (status == null) return new String[]{};

        return switch (status) {
            case "ASSIGNED" -> new String[]{"INITIATED"};
            case "INITIATED" -> new String[]{"IN_PROGRESS"};
            case "IN_PROGRESS" -> new String[]{"COMPLETED"};
            case "COMPLETED" -> new String[]{"PENDING_REVIEW"};
            case "PENDING_REVIEW" -> new String[]{"APPROVED", "REJECTED"};
            default -> new String[]{};
        };
    }

    // =========================
    // GETTERS & SETTERS
    // =========================

    public String getTaskAssignCode() { return taskAssignCode; }
    public void setTaskAssignCode(String taskAssignCode) { this.taskAssignCode = taskAssignCode; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }

    // ✅ ADDED GETTER AND SETTER FOR DEADLINE
    public LocalDateTime getTaskDeadline() { return taskDeadline; }
    public void setTaskDeadline(LocalDateTime taskDeadline) { this.taskDeadline = taskDeadline; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public String getStaffFirstName() { return staffFirstName; }
    public void setStaffFirstName(String staffFirstName) { this.staffFirstName = staffFirstName; }

    public String getStaffOtherName() { return staffOtherName; }
    public void setStaffOtherName(String staffOtherName) { this.staffOtherName = staffOtherName; }

    public String getStaffBranch() { return staffBranch; }
    public void setStaffBranch(String staffBranch) { this.staffBranch = staffBranch; }

    public String getStaffDepartment() { return staffDepartment; }
    public void setStaffDepartment(String staffDepartment) { this.staffDepartment = staffDepartment; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public String getStatus() { return status; }

    public void setStatus(String status) {
        this.status = status;
        this.statusDisplayName = getDisplayNameFromStatus(status);
        this.statusBadgeClass = getBadgeClassFromStatus(status);
    }

    public String getStatusDisplayName() { return statusDisplayName; }
    public String getStatusBadgeClass() { return statusBadgeClass; }

    // =========================
    // UI HELPERS
    // =========================

    public String getStaffFullName() {
        String name = ((staffFirstName != null ? staffFirstName : "") + " " +
                (staffOtherName != null ? staffOtherName : "")).trim();

        if (name.isEmpty()) name = "N/A";

        String branch = staffBranch != null ? staffBranch : "N/A";
        String dept = staffDepartment != null ? staffDepartment : "N/A";

        return name + " - " + branch + " / " + dept;
    }

    public boolean isEditable() {
        return !isFinalState();
    }

    public int getStatusProgress() {
        if (status == null) return 0;

        return switch (status) {
            case "ASSIGNED" -> 0;
            case "INITIATED" -> 20;
            case "IN_PROGRESS" -> 50;
            case "COMPLETED" -> 80;
            case "PENDING_REVIEW" -> 90;
            case "APPROVED", "REJECTED" -> 100;
            default -> 0;
        };
    }

    // ✅ ADDED DEADLINE HELPER METHODS
    public boolean isOverdue() {
        return taskDeadline != null && LocalDateTime.now().isAfter(taskDeadline);
    }

    public String getDeadlineStatus() {
        if (taskDeadline == null) return "none";
        if (isOverdue()) return "overdue";

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warning = taskDeadline.minusDays(3);

        if (now.isAfter(warning)) return "warning";
        return "ok";
    }

    @Override
    public String toString() {
        return "TaskAssignmentDTOResponse{" +
                "taskAssignCode='" + taskAssignCode + '\'' +
                ", taskId=" + taskId +
                ", taskDescription='" + taskDescription + '\'' +
                ", taskDeadline=" + taskDeadline +
                ", staffId=" + staffId +
                ", staffFullName='" + getStaffFullName() + '\'' +
                ", assignedAt=" + assignedAt +
                ", status='" + status + '\'' +
                '}';
    }
}