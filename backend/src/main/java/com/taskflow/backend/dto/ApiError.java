package com.taskflow.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private String error;
    private Map<String, String> fields;

    public ApiError(String error) {
        this.error = error;
    }

    public ApiError(String error, Map<String, String> fields) {
        this.error = error;
        this.fields = fields;
    }

    public String getError() { return error; }
    public Map<String, String> getFields() { return fields; }
}