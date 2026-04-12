package com.taskflow.backend.dto;

import com.taskflow.backend.model.Project;
import com.taskflow.backend.model.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ProjectDetailResponse {

    private UUID id;
    private String name;
    private String description;
    private UUID ownerId;
    private LocalDateTime createdAt;
    private List<TaskResponse> tasks;

    public ProjectDetailResponse(Project project, List<Task> tasks) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.ownerId = project.getOwner().getId();
        this.createdAt = project.getCreatedAt();
        this.tasks = tasks.stream().map(TaskResponse::new).toList();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public UUID getOwnerId() { return ownerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<TaskResponse> getTasks() { return tasks; }

    public static class TaskResponse {
        private UUID id;
        private String title;
        private String description;
        private String status;
        private String priority;
        private UUID assigneeId;
        private UUID createdById;
        private String dueDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public TaskResponse(Task task) {
            this.id = task.getId();
            this.title = task.getTitle();
            this.description = task.getDescription();
            this.status = task.getStatus();
            this.priority = task.getPriority();
            this.assigneeId = task.getAssignee() != null ? task.getAssignee().getId() : null;
            this.createdById = task.getCreatedBy() != null ? task.getCreatedBy().getId() : null;
            this.dueDate = task.getDueDate() != null ? task.getDueDate().toString() : null;
            this.createdAt = task.getCreatedAt();
            this.updatedAt = task.getUpdatedAt();
        }

        public UUID getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
        public String getPriority() { return priority; }
        public UUID getAssigneeId() { return assigneeId; }
        public UUID getCreatedById() { return createdById; }
        public String getDueDate() { return dueDate; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }
}