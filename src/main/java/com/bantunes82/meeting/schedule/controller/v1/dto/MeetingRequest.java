package com.bantunes82.meeting.schedule.controller.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for converting a time slot into a meeting.
 */
public record MeetingRequest(
        @NotBlank(message = "Title is required")
        String title,

        String description,

        @NotEmpty(message = "At least one participant is required")
        Set<UUID> participantIds
) {
}
