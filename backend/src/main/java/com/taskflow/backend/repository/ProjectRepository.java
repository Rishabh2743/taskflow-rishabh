package com.taskflow.backend.repository;

import com.taskflow.backend.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN Task t ON t.project = p " +
           "WHERE p.owner.id = :userId OR t.assignee.id = :userId")
    List<Project> findProjectsForUser(@Param("userId") UUID userId);
}