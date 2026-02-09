package com.bantunes82.meeting.schedule.controller;

import com.bantunes82.meeting.schedule.controller.v1.dto.MeetingRequest;
import com.bantunes82.meeting.schedule.controller.v1.dto.MeetingResponse;
import com.bantunes82.meeting.schedule.controller.v1.mapper.MeetingMapper;
import com.bantunes82.meeting.schedule.service.MeetingService;
import jakarta.validation.Valid;
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
    @PostMapping("/time-slots/{slotId}/meetings")
    public ResponseEntity<MeetingResponse> createMeeting(
            @PathVariable UUID userId,
            @PathVariable UUID slotId,
            @Valid @RequestBody MeetingRequest request) {
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
    @GetMapping("/meetings/{meetingId}")
    public ResponseEntity<MeetingResponse> getMeeting(
            @PathVariable UUID userId,
            @PathVariable UUID meetingId) {
        var meeting = meetingService.getMeeting(userId, meetingId);
        return ResponseEntity.ok(meetingMapper.toResponse(meeting));
    }
}
