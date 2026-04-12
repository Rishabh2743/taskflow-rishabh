package com.taskflow.backend.service;

import com.taskflow.backend.exception.DuplicateResourceException;
import com.taskflow.backend.exception.UnauthorizedException;
import com.taskflow.backend.exception.ValidationException;
import com.taskflow.backend.model.User;
import com.taskflow.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    public User register(String name, String email, String password) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (name == null || name.isBlank()) errors.put("name", "is required");
        if (email == null || email.isBlank()) errors.put("email", "is required");
        if (password == null || password.length() < 6)
            errors.put("password", "must be at least 6 characters");
        if (!errors.isEmpty()) throw new ValidationException(errors);

        if (userRepository.findByEmail(email.trim().toLowerCase()).isPresent())
            throw new DuplicateResourceException("email already exists");

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(encoder.encode(password));
        User saved = userRepository.save(user);
        log.info("User registered: {}", saved.getEmail());
        return saved;
    }

    public User login(String email, String password) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (email == null || email.isBlank()) errors.put("email", "is required");
        if (password == null || password.isBlank()) errors.put("password", "is required");
        if (!errors.isEmpty()) throw new ValidationException(errors);

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("invalid credentials"));

        if (!encoder.matches(password, user.getPassword()))
            throw new UnauthorizedException("invalid credentials");

        log.info("User logged in: {}", user.getEmail());
        return user;
    }
}