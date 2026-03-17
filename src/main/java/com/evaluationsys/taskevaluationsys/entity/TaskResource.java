package com.evaluationsys.taskevaluationsys.entity;

import jakarta.persistence.*;

@Entity
public class TaskResource {

    @EmbeddedId
    private TaskResourceId id;

    @ManyToOne
    @MapsId("task_id")
    @JoinColumn(
            name = "Task",
            foreignKey = @ForeignKey(name = "fk_taskresources_task")
    )
    private Task task;

    @ManyToOne
    @MapsId("resource_id")
    @JoinColumn(
            name = "Resource",
            foreignKey = @ForeignKey(name = "fk_taskresource_resource")

    )

    private Resource resource;

    public TaskResource(TaskResourceId id, Resource resource, Task task) {
        this.id = id;
        this.resource = resource;
        this.task = task;
    }

    public TaskResource() {

    }

    public TaskResourceId getId() {
        return id;
    }

    public void setId(TaskResourceId id) {
        this.id = id;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
