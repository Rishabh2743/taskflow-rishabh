package com.taskflow.backend.exception;

import java.util.Map;

public class ValidationException extends RuntimeException {

    private final Map<String, String> fields;

    public ValidationException(Map<String, String> fields) {
        super("validation failed");
        this.fields = fields;
    }

    public Map<String, String> getFields() { return fields; }
}