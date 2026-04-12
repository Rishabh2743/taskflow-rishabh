package com.taskflow.backend.controller;

import com.taskflow.backend.dto.ProjectDetailResponse;
import com.taskflow.backend.exception.UnauthorizedException;
import com.taskflow.backend.model.Project;
import com.taskflow.backend.service.ProjectService;
import com.taskflow.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
public class ProjectController {

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

    @GetMapping
    public ResponseEntity<List<Project>> list(HttpServletRequest request) {
        return ResponseEntity.ok(projectService.getProjectsForUser(getUserId(request)));
    }

    @PostMapping
    public ResponseEntity<Project> create(@RequestBody Map<String, String> body,
                                          HttpServletRequest request) {
        Project p = projectService.createProject(
                getUserId(request),
                body.get("name"),
                body.get("description"));
        return ResponseEntity.status(201).body(p);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectDetail(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Project> update(@PathVariable UUID id,
                                          @RequestBody Map<String, String> body,
                                          HttpServletRequest request) {
        Project p = projectService.updateProject(
                getUserId(request), id,
                body.get("name"),
                body.get("description"));
        return ResponseEntity.ok(p);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       HttpServletRequest request) {
        projectService.deleteProject(getUserId(request), id);
        return ResponseEntity.noContent().build();
    }
}