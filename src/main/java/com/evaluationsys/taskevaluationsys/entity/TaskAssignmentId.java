package com.evaluationsys.taskevaluationsys.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TaskAssignmentId implements Serializable {

    private Long staffId;
    private Long taskId;

    public TaskAssignmentId() {}

    public TaskAssignmentId(Long staffId, Long taskId) {
        this.staffId = staffId;
        this.taskId = taskId;
    }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskAssignmentId that)) return false;
        return Objects.equals(staffId, that.staffId) &&
                Objects.equals(taskId, that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(staffId, taskId);
    }
}