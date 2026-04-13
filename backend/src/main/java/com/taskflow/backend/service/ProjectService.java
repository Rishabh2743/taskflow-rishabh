package com.taskflow.backend.service;

import com.taskflow.backend.dto.PagedResponse;
import com.taskflow.backend.dto.ProjectDetailResponse;
import com.taskflow.backend.dto.ProjectResponse;
import com.taskflow.backend.exception.ResourceNotFoundException;
import com.taskflow.backend.exception.ValidationException;
import com.taskflow.backend.model.Project;
import com.taskflow.backend.model.User;
import com.taskflow.backend.repository.ProjectRepository;
import com.taskflow.backend.repository.TaskRepository;
import com.taskflow.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    public PagedResponse<ProjectResponse> getProjectsForUser(UUID userId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        Page<Project> result = projectRepository.findProjectsForUser(userId, pageable);
        List<ProjectResponse> data = result.getContent()
                .stream()
                .map(ProjectResponse::new)
                .toList();
        return new PagedResponse<>(data, page, limit, result.getTotalElements());
    }

    @Transactional
    public Project createProject(UUID userId, String name, String description) {
        if (name == null || name.isBlank())
            throw new ValidationException(Map.of("name", "is required"));

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));

        Project project = new Project();
        project.setName(name.trim());
        project.setDescription(description);
        project.setOwner(owner);
        Project saved = projectRepository.save(project);
        log.info("Project created: {} by user: {}", saved.getId(), userId);
        return saved;
    }

    public ProjectDetailResponse getProjectDetail(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("project not found"));
        var tasks = taskRepository.findByProjectId(projectId);
        return new ProjectDetailResponse(project, tasks);
    }

    public Project getRawProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("project not found"));
    }

    @Transactional
    public Project updateProject(UUID userId, UUID projectId, String name, String description) {
        Project project = getRawProject(projectId);

        if (!project.getOwner().getId().equals(userId))
            throw new SecurityException("forbidden");

        if (name != null && !name.isBlank()) project.setName(name.trim());
        if (description != null) project.setDescription(description);
        Project saved = projectRepository.save(project);
        log.info("Project updated: {}", projectId);
        return saved;
    }

    @Transactional
    public void deleteProject(UUID userId, UUID projectId) {
        Project project = getRawProject(projectId);

        if (!project.getOwner().getId().equals(userId))
            throw new SecurityException("forbidden");

        projectRepository.delete(project);
        log.info("Project deleted: {}", projectId);
    }
}