package com.bantunes82.meeting.schedule.controller;

import com.bantunes82.meeting.schedule.controller.v1.dto.TimeSlotRequest;
import com.bantunes82.meeting.schedule.controller.v1.dto.TimeSlotResponse;
import com.bantunes82.meeting.schedule.controller.v1.dto.UpdateTimeSlotStatusRequest;
import com.bantunes82.meeting.schedule.controller.v1.mapper.TimeSlotMapper;
import com.bantunes82.meeting.schedule.model.TimeSlotStatus;
import com.bantunes82.meeting.schedule.service.TimeSlotService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger log = LoggerFactory.getLogger(TimeSlotController.class);

    private final TimeSlotService timeSlotService;
    private final TimeSlotMapper timeSlotMapper;

    public TimeSlotController(TimeSlotService timeSlotService, TimeSlotMapper timeSlotMapper) {
        this.timeSlotService = timeSlotService;
        this.timeSlotMapper = timeSlotMapper;
    }

    /**
     * Creates a new time slot for the specified user.
     *
     * @param userId  the user's UUID
     * @param request the creation request with start and end times
     * @return the created time slot (201 Created)
     */
    @Timed(value = "timeslot.create.controller", description = "Time taken to create a time slot", histogram = true, percentiles = {0.5,0.75,0.95,0.98,0.99})
    @PostMapping
    public ResponseEntity<TimeSlotResponse> createTimeSlot(
            @PathVariable UUID userId,
            @Valid @RequestBody TimeSlotRequest request) {
        log.info("Creating time slot for user {} from {} to {}", userId, request.startTime(), request.endTime());
        var timeSlot = timeSlotService.createTimeSlot(userId, request.startTime(), request.endTime());
        return ResponseEntity.status(HttpStatus.CREATED).body(timeSlotMapper.toResponse(timeSlot));
    }

    /**
     * Gets a single time slot by ID.
     *
     * @param userId the user's UUID
     * @param slotId the time slot's UUID
     * @return the time slot
     */
    @Timed(value = "timeslot.get.contoller", description = "Time taken to get a time slot", histogram = true, percentiles = {0.5,0.75,0.95,0.98,0.99})
    @GetMapping("/{slotId}")
    public ResponseEntity<TimeSlotResponse> getTimeSlot(
            @PathVariable UUID userId,
            @PathVariable UUID slotId) {
        log.info("Getting time slot {} for user {}", slotId, userId);
        var timeSlot = timeSlotService.getTimeSlot(userId, slotId);
        return ResponseEntity.ok(timeSlotMapper.toResponse(timeSlot));
    }

    /**
     * Updates a time slot's start and end times.
     *
     * @param userId  the user's UUID
     * @param slotId  the time slot's UUID
     * @param request the update request
     * @return the updated time slot
     */
    @Timed(value = "timeslot.update.controller", description = "Time taken to update a time slot", histogram = true, percentiles = {0.5,0.75,0.95,0.98,0.99})
    @PutMapping("/{slotId}")
    public ResponseEntity<TimeSlotResponse> updateTimeSlot(
            @PathVariable UUID userId,
            @PathVariable UUID slotId,
            @Valid @RequestBody TimeSlotRequest request) {
        log.info("Updating time slot {} for user {} to new times: {} - {}", slotId, userId, request.startTime(), request.endTime());
        var timeSlot = timeSlotService.updateTimeSlot(userId, slotId, request.startTime(), request.endTime());
        return ResponseEntity.ok(timeSlotMapper.toResponse(timeSlot));
    }

    /**
     * Deletes a time slot.
     *
     * @param userId the user's UUID
     * @param slotId the time slot's UUID
     */
    @Timed(value = "timeslot.delete.controller", description = "Time taken to delete a time slot", histogram = true, percentiles = {0.5,0.75,0.95,0.98,0.99})
    @DeleteMapping("/{slotId}")
    public ResponseEntity<Void> deleteTimeSlot(
            @PathVariable UUID userId,
            @PathVariable UUID slotId) {
        log.info("Deleting time slot {} for user {}", slotId, userId);
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
    @Timed(value = "timeslot.update-status.controller", description = "Time taken to update a time slot's status", histogram = true, percentiles = {0.5,0.75,0.95,0.98,0.99})
    @PatchMapping("/{slotId}/status")
    public ResponseEntity<TimeSlotResponse> updateTimeSlotStatus(
            @PathVariable UUID userId,
            @PathVariable UUID slotId,
            @Valid @RequestBody UpdateTimeSlotStatusRequest request) {
        log.info("Updating status of time slot {} for user {} to {}", slotId, userId, request.status());
        var modelStatus = TimeSlotStatus.valueOf(request.status().name());
        var timeSlot = timeSlotService.updateTimeSlotStatus(userId, slotId, modelStatus);
        return ResponseEntity.ok(timeSlotMapper.toResponse(timeSlot));
    }
}
