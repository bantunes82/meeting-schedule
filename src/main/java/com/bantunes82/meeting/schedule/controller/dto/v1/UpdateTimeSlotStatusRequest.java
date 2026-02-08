package com.bantunes82.meeting.schedule.controller.dto.v1;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for changing a time slot's status (FREE/BUSY).
 */
public record UpdateTimeSlotStatusRequest(
                @NotNull(message = "Status is required") TimeSlotStatus status) {
}
