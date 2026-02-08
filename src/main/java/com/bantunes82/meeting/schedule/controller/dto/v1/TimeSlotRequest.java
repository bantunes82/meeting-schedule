package com.bantunes82.meeting.schedule.controller.dto.v1;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Request DTO for creating a new time slot.
 */
public record TimeSlotRequest(
        @NotNull(message = "Start time is required")
        @Future(message = "Start time must be in the future")
        Instant startTime,

        @NotNull(message = "End time is required")
        @Future(message = "End time must be in the future")
        Instant endTime
) {
}
