package com.taskflow.backend.exception;

import com.taskflow.backend.dto.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationException ex) {
        log.warn("Validation failed: {}", ex.getFields());
        return ResponseEntity.badRequest()
                .body(new ApiError("validation failed", ex.getFields()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex) {
        log.warn("Unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(401)
                .body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiError> handleForbidden(SecurityException ex) {
        log.warn("Forbidden: {}", ex.getMessage());
        return ResponseEntity.status(403)
                .body(new ApiError("forbidden"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return ResponseEntity.status(404)
                .body(new ApiError("not found"));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return ResponseEntity.status(409)
                .body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(500)
                .body(new ApiError("internal server error"));
    }
}