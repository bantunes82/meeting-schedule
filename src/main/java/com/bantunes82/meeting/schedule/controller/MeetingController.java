package com.bantunes82.meeting.schedule.controller;

import com.bantunes82.meeting.schedule.controller.v1.dto.MeetingRequest;
import com.bantunes82.meeting.schedule.controller.v1.dto.MeetingResponse;
import com.bantunes82.meeting.schedule.controller.v1.mapper.MeetingMapper;
import com.bantunes82.meeting.schedule.service.MeetingService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for managing meetings.
 */
@RestController
@RequestMapping("/api/{version}/users/{userId}")
public class MeetingController {

    private final Logger log = LoggerFactory.getLogger(MeetingController.class);

    private final MeetingService meetingService;
    private final MeetingMapper meetingMapper;

    public MeetingController(MeetingService meetingService, MeetingMapper meetingMapper) {
        this.meetingService = meetingService;
        this.meetingMapper = meetingMapper;
    }

    /**
     * Converts a free time slot into a meeting.
     *
     * @param userId  the user's UUID
     * @param slotId  the time slot's UUID
     * @param request the meeting creation request
     * @return the created meeting (201 Created)
     */
    @Timed(value = "meeting.create.controller", description = "Time taken to create a meeting", histogram = true, percentiles = {0.5,0.75,0.95,0.98,0.99})
    @PostMapping("/time-slots/{slotId}/meetings")
    public ResponseEntity<MeetingResponse> createMeeting(
            @PathVariable UUID userId,
            @PathVariable UUID slotId,
            @Valid @RequestBody MeetingRequest request) {
        log.info("Creating meeting for user {} in slot {} with title '{}'", userId, slotId, request.title());
        var meeting = meetingService.createMeeting(userId, slotId, request.title(), request.description(),
                request.participantIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(meetingMapper.toResponse(meeting));
    }

    /**
     * Gets a single meeting with full details.
     *
     * @param userId    the user's UUID
     * @param meetingId the meeting's UUID
     * @return the meeting with participants
     */
    @Timed(value = "meeting.get.controller", description = "Time taken to get a meeting", histogram = true, percentiles = {0.5,0.75,0.95,0.98,0.99})
    @GetMapping("/meetings/{meetingId}")
    public ResponseEntity<MeetingResponse> getMeeting(
            @PathVariable UUID userId,
            @PathVariable UUID meetingId) {
        log.info("Getting meeting {} for user {}", meetingId, userId);
        var meeting = meetingService.getMeeting(userId, meetingId);
        return ResponseEntity.ok(meetingMapper.toResponse(meeting));
    }
}
