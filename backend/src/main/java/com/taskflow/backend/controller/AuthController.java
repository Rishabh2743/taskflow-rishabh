package com.taskflow.backend.controller;

import com.taskflow.backend.dto.AuthRequest;
import com.taskflow.backend.dto.AuthResponse;
import com.taskflow.backend.model.User;
import com.taskflow.backend.service.AuthService;
import com.taskflow.backend.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        User user = authService.register(request.name, request.email, request.password);
        String token = JwtUtil.generateToken(user.getId(), user.getEmail());
        return ResponseEntity.status(201).body(new AuthResponse(token, user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        User user = authService.login(request.email, request.password);
        String token = JwtUtil.generateToken(user.getId(), user.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail()));
    }
}