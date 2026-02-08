package com.bantunes82.meeting.schedule.controller;

import com.bantunes82.meeting.schedule.controller.dto.v1.TimeSlotRequest;
import com.bantunes82.meeting.schedule.controller.dto.v1.TimeSlotResponse;
import com.bantunes82.meeting.schedule.controller.dto.v1.UpdateTimeSlotStatusRequest;
import com.bantunes82.meeting.schedule.model.TimeSlotStatus;
import com.bantunes82.meeting.schedule.service.TimeSlotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for managing time slots.
 */
@RestController
@RequestMapping("/api/{version}/users/{userId}/time-slots")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    /**
     * Creates a new time slot for the specified user.
     *
     * @param userId  the user's UUID
     * @param request the creation request with start and end times
     * @return the created time slot (201 Created)
     */
    @PostMapping
    public ResponseEntity<TimeSlotResponse> createTimeSlot(
            @PathVariable UUID userId,
            @Valid @RequestBody TimeSlotRequest request) {
        var timeSlot = timeSlotService.createTimeSlot(userId, request.startTime(), request.endTime());
        return ResponseEntity.status(HttpStatus.CREATED).body(TimeSlotResponse.from(timeSlot));
    }

    /**
     * Gets a single time slot by ID.
     *
     * @param userId the user's UUID
     * @param slotId the time slot's UUID
     * @return the time slot
     */
    @GetMapping("/{slotId}")
    public ResponseEntity<TimeSlotResponse> getTimeSlot(
            @PathVariable UUID userId,
            @PathVariable UUID slotId) {
        var timeSlot = timeSlotService.getTimeSlot(userId, slotId);
        return ResponseEntity.ok(TimeSlotResponse.from(timeSlot));
    }

    /**
     * Updates a time slot's start and end times.
     *
     * @param userId  the user's UUID
     * @param slotId  the time slot's UUID
     * @param request the update request
     * @return the updated time slot
     */
    @PutMapping("/{slotId}")
    public ResponseEntity<TimeSlotResponse> updateTimeSlot(
            @PathVariable UUID userId,
            @PathVariable UUID slotId,
            @Valid @RequestBody TimeSlotRequest request) {
        var timeSlot = timeSlotService.updateTimeSlot(userId, slotId, request.startTime(), request.endTime());
        return ResponseEntity.ok(TimeSlotResponse.from(timeSlot));
    }

    /**
     * Deletes a time slot.
     *
     * @param userId the user's UUID
     * @param slotId the time slot's UUID
     */
    @DeleteMapping("/{slotId}")
    public ResponseEntity<Void> deleteTimeSlot(
            @PathVariable UUID userId,
            @PathVariable UUID slotId) {
        timeSlotService.deleteTimeSlot(userId, slotId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Changes a time slot's status (FREE/BUSY).
     *
     * @param userId  the user's UUID
     * @param slotId  the time slot's UUID
     * @param request the status update request
     * @return the updated time slot
     */
    @PatchMapping("/{slotId}/status")
    public ResponseEntity<TimeSlotResponse> updateTimeSlotStatus(
            @PathVariable UUID userId,
            @PathVariable UUID slotId,
            @Valid @RequestBody UpdateTimeSlotStatusRequest request) {
        var modelStatus = TimeSlotStatus.valueOf(request.status().name());
        var timeSlot = timeSlotService.updateTimeSlotStatus(userId, slotId, modelStatus);
        return ResponseEntity.ok(TimeSlotResponse.from(timeSlot));
    }
}
