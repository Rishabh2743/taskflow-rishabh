package com.taskflow.backend.controller;

import com.taskflow.backend.dto.PagedResponse;
import com.taskflow.backend.dto.ProjectDetailResponse;
import com.taskflow.backend.dto.ProjectRequest;
import com.taskflow.backend.dto.ProjectResponse;
import com.taskflow.backend.exception.UnauthorizedException;
import com.taskflow.backend.model.Project;
import com.taskflow.backend.service.ProjectService;
import com.taskflow.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    private UUID getUserId(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer "))
            throw new UnauthorizedException("missing or invalid token");
        return JwtUtil.extractUserId(auth.substring(7));
    }

    private int sanitizePage(int page) {
        return Math.max(1, page);
    }

    private int sanitizeLimit(int limit) {
        return Math.min(Math.max(1, limit), MAX_LIMIT);
    }

    // GET /projects?page=1&limit=10
    @GetMapping
    public ResponseEntity<PagedResponse<ProjectResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {

        PagedResponse<ProjectResponse> response = projectService.getProjectsForUser(
                getUserId(request),
                sanitizePage(page),
                sanitizeLimit(limit));

        return ResponseEntity.ok(response);
    }

    // POST /projects
    @PostMapping
    public ResponseEntity<ProjectResponse> create(
            @RequestBody ProjectRequest body,
            HttpServletRequest request) {

        Project p = projectService.createProject(
                getUserId(request),
                body.getName(),
                body.getDescription());

        return ResponseEntity.status(201).body(new ProjectResponse(p));
    }

    // GET /projects/:id
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectDetail(id));
    }

    // PATCH /projects/:id
    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(
            @PathVariable UUID id,
            @RequestBody ProjectRequest body,
            HttpServletRequest request) {

        Project p = projectService.updateProject(
                getUserId(request), id,
                body.getName(),
                body.getDescription());

        return ResponseEntity.ok(new ProjectResponse(p));
    }

    // DELETE /projects/:id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            HttpServletRequest request) {

        projectService.deleteProject(getUserId(request), id);
        return ResponseEntity.noContent().build();
    }
}