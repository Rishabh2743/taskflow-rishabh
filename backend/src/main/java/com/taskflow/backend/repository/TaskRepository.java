package com.taskflow.backend.repository;

import com.taskflow.backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByProjectId(UUID projectId);
    List<Task> findByProjectIdAndStatus(UUID projectId, String status);
    List<Task> findByProjectIdAndAssigneeId(UUID projectId, UUID assigneeId);
    List<Task> findByProjectIdAndStatusAndAssigneeId(UUID projectId, String status, UUID assigneeId);
}