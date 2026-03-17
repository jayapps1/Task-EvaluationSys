package com.evaluationsys.taskevaluationsys.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;


@Embeddable
public class TaskResourceId implements Serializable {

    private Long task_id;
    private Long resource_id;

    public TaskResourceId(Long task_id, Long resource_id) {
        this.task_id = task_id;
        this.resource_id = resource_id;
    }

    public TaskResourceId() {

    }

    public Long getTask_id() {
        return task_id;
    }

    public void setTask_id(Long task_id) {
        this.task_id = task_id;
    }

    public Long getResource_id() {
        return resource_id;
    }

    public void setResource_id(Long resource_id) {
        this.resource_id = resource_id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TaskResourceId that = (TaskResourceId) o;
        return Objects.equals(task_id, that.task_id) && Objects.equals(resource_id, that.resource_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(task_id, resource_id);
    }
}
