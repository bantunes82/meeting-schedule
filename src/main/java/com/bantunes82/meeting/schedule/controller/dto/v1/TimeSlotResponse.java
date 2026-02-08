package com.bantunes82.meeting.schedule.controller.dto.v1;

import com.bantunes82.meeting.schedule.model.TimeSlot;

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
    /**
     * Creates a response from a TimeSlot entity.
     */
    public static TimeSlotResponse from(TimeSlot timeSlot) {
        return new TimeSlotResponse(
                timeSlot.getId(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                TimeSlotStatus.valueOf(timeSlot.getStatus().name()),
                timeSlot.getMeeting() != null,
                timeSlot.getCreatedAt(),
                timeSlot.getUpdatedAt());
    }
}
