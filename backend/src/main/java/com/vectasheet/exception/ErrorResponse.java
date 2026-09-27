package com.vectasheet.exception;

import java.time.Instant;
import java.util.Map;

public class ErrorResponse {
    private int status;
    private String message;
    private Instant timestamp = Instant.now();
    private Map<String, String> fieldErrors;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public ErrorResponse(int status, String message, Map<String, String> fieldErrors) {
        this.status = status;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
    public Map<String, String> getFieldErrors() { return fieldErrors; }
}
