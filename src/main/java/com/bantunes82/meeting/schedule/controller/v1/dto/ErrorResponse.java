package com.bantunes82.meeting.schedule.controller.v1.dto;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response DTO for API errors.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        List<String> details,
        Instant timestamp
) {
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, List.of(), Instant.now());
    }

    public ErrorResponse(int status, String error, String message, List<String> details) {
        this(status, error, message, details, Instant.now());
    }
}
