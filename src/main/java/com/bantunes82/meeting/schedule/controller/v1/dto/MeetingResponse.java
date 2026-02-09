package com.bantunes82.meeting.schedule.controller.v1.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for a meeting with full details.
 */
public record MeetingResponse(
                UUID id,
                UUID timeSlotId,
                Instant startTime,
                Instant endTime,
                String title,
                String description,
                Set<ParticipantResponse> participants,
                Instant createdAt,
                Instant updatedAt) {

        public record ParticipantResponse(UUID id, String name, String email) {
        }
}
