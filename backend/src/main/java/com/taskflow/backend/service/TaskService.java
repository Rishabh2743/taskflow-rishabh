package com.taskflow.backend.service;

import com.taskflow.backend.exception.ResourceNotFoundException;
import com.taskflow.backend.exception.ValidationException;
import com.taskflow.backend.model.Project;
import com.taskflow.backend.model.Task;
import com.taskflow.backend.model.User;
import com.taskflow.backend.repository.ProjectRepository;
import com.taskflow.backend.repository.TaskRepository;
import com.taskflow.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public List<Task> getTasks(UUID projectId, String status, UUID assigneeId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("project not found"));

        if (status != null && assigneeId != null)
            return taskRepository.findByProjectIdAndStatusAndAssigneeId(projectId, status, assigneeId);
        if (status != null)
            return taskRepository.findByProjectIdAndStatus(projectId, status);
        if (assigneeId != null)
            return taskRepository.findByProjectIdAndAssigneeId(projectId, assigneeId);
        return taskRepository.findByProjectId(projectId);
    }
    @Transactional
    public Task createTask(UUID projectId, UUID creatorId, String title, String description,
                           String status, String priority, UUID assigneeId, LocalDate dueDate) {
        if (title == null || title.isBlank())
            throw new ValidationException(Map.of("title", "is required"));

        validateStatus(status);
        validatePriority(priority);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("project not found"));

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));

        Task task = new Task();
        task.setTitle(title.trim());
        task.setDescription(description);
        task.setProject(project);
        task.setCreatedBy(creator);
        if (status != null) task.setStatus(status);
        if (priority != null) task.setPriority(priority);
        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("assignee not found"));
            task.setAssignee(assignee);
        }
        task.setDueDate(dueDate);
        Task saved = taskRepository.save(task);
        log.info("Task created: {} in project: {}", saved.getId(), projectId);
        return saved;
    }
    @Transactional
    public Task updateTask(UUID taskId, String title, String description,
                           String status, String priority, UUID assigneeId, LocalDate dueDate) {
        validateStatus(status);
        validatePriority(priority);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("task not found"));

        if (title != null && !title.isBlank()) task.setTitle(title.trim());
        if (description != null) task.setDescription(description);
        if (status != null) task.setStatus(status);
        if (priority != null) task.setPriority(priority);
        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("assignee not found"));
            task.setAssignee(assignee);
        }
        if (dueDate != null) task.setDueDate(dueDate);
        Task saved = taskRepository.save(task);
        log.info("Task updated: {}", taskId);
        return saved;
    }

    // spec: project owner OR task creator can delete
    @Transactional
    public void deleteTask(UUID userId, UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("task not found"));

        UUID ownerId = task.getProject().getOwner().getId();
        UUID creatorId = task.getCreatedBy() != null ? task.getCreatedBy().getId() : null;

        if (!userId.equals(ownerId) && !userId.equals(creatorId))
            throw new SecurityException("forbidden");

        taskRepository.delete(task);
        log.info("Task deleted: {}", taskId);
    }

    private void validateStatus(String status) {
        if (status != null && !List.of("todo", "in_progress", "done").contains(status))
            throw new ValidationException(Map.of("status", "must be one of: todo, in_progress, done"));
    }

    private void validatePriority(String priority) {
        if (priority != null && !List.of("low", "medium", "high").contains(priority))
            throw new ValidationException(Map.of("priority", "must be one of: low, medium, high"));
    }
}