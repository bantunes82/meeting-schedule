package com.bantunes82.meeting.schedule.controller.v1.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for a time slot.
 */
public record TimeSlotResponse(
        UUID id,
        Instant startTime,
        Instant endTime,
        TimeSlotStatus status,
        boolean hasMeeting,
        Instant createdAt,
        Instant updatedAt) {

}
