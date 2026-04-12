package com.taskflow.backend.controller;

import com.taskflow.backend.exception.UnauthorizedException;
import com.taskflow.backend.model.Task;
import com.taskflow.backend.service.TaskService;
import com.taskflow.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    private UUID getUserId(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer "))
            throw new UnauthorizedException("missing or invalid token");
        return JwtUtil.extractUserId(auth.substring(7));
    }

    @GetMapping("/projects/{id}/tasks")
    public ResponseEntity<List<Task>> list(@PathVariable UUID id,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(required = false) UUID assignee) {
        return ResponseEntity.ok(taskService.getTasks(id, status, assignee));
    }

    @PostMapping("/projects/{id}/tasks")
    public ResponseEntity<Task> create(@PathVariable UUID id,
                                       @RequestBody Map<String, String> body,
                                       HttpServletRequest request) {
        UUID assigneeId = body.get("assignee_id") != null
                ? UUID.fromString(body.get("assignee_id")) : null;
        LocalDate dueDate = body.get("due_date") != null
                ? LocalDate.parse(body.get("due_date")) : null;

        Task t = taskService.createTask(
                id,
                getUserId(request),
                body.get("title"),
                body.get("description"),
                body.get("status"),
                body.get("priority"),
                assigneeId,
                dueDate);
        return ResponseEntity.status(201).body(t);
    }

    @PatchMapping("/tasks/{id}")
    public ResponseEntity<Task> update(@PathVariable UUID id,
                                       @RequestBody Map<String, String> body) {
        UUID assigneeId = body.get("assignee_id") != null
                ? UUID.fromString(body.get("assignee_id")) : null;
        LocalDate dueDate = body.get("due_date") != null
                ? LocalDate.parse(body.get("due_date")) : null;

        Task t = taskService.updateTask(
                id,
                body.get("title"),
                body.get("description"),
                body.get("status"),
                body.get("priority"),
                assigneeId,
                dueDate);
        return ResponseEntity.ok(t);
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       HttpServletRequest request) {
        taskService.deleteTask(getUserId(request), id);
        return ResponseEntity.noContent().build();
    }
}