package com.bantunes82.meeting.schedule.service;

import com.bantunes82.meeting.schedule.exception.ResourceNotFoundException;
import com.bantunes82.meeting.schedule.exception.TimeSlotNotAvailableException;
import com.bantunes82.meeting.schedule.model.Calendar;
import com.bantunes82.meeting.schedule.model.Meeting;
import com.bantunes82.meeting.schedule.model.TimeSlot;
import com.bantunes82.meeting.schedule.model.TimeSlotStatus;
import com.bantunes82.meeting.schedule.model.User;
import com.bantunes82.meeting.schedule.repository.MeetingRepository;
import com.bantunes82.meeting.schedule.repository.TimeSlotRepository;
import com.bantunes82.meeting.schedule.repository.UserRepository;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service for managing meetings.
 */
@Service
@Transactional(readOnly = true)
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final CalendarService calendarService;
    private final UserRepository userRepository;

    public MeetingService(MeetingRepository meetingRepository,
            TimeSlotRepository timeSlotRepository,
            CalendarService calendarService,
            UserRepository userRepository) {
        this.meetingRepository = meetingRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.calendarService = calendarService;
        this.userRepository = userRepository;
    }

    /**
     * Converts a free time slot into a meeting.
     * Validates the slot is FREE, participants exist, then sets slot to BUSY and
     * creates the meeting.
     *
     * @param userId  the slot owner's UUID
     * @param slotId  the time slot UUID to convert
     * @param title   the meeting title
     * @param description the meeting description
     * @param participantIds the UUIDs of participants to invite
     * @return the created meeting
     */
    @Observed(name="meeting.create")
    @Transactional
    public Meeting createMeeting(UUID userId, UUID slotId, String title, String description, Set<UUID> participantIds) {
        Calendar calendar = calendarService.findByUserId(userId);
        TimeSlot timeSlot = timeSlotRepository.findByIdAndCalendarId(slotId, calendar.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found with id: " + slotId));

        if (timeSlot.getStatus() != TimeSlotStatus.FREE) {
            throw new TimeSlotNotAvailableException("Time slot is not available for booking");
        }

        if (timeSlot.getMeeting() != null) {
            throw new TimeSlotNotAvailableException("Time slot already has a meeting");
        }

        Set<User> participants = resolveParticipants(participantIds);

        timeSlot.setStatus(TimeSlotStatus.BUSY);

        Meeting meeting = new Meeting(timeSlot, title, description, participants);
        meeting = meetingRepository.save(meeting);
        timeSlotRepository.save(timeSlot);

        return meeting;
    }

    /**
     * Gets a single meeting with full details, validating ownership.
     *
     * @param userId    the owner's UUID
     * @param meetingId the meeting UUID
     * @return the meeting with full details
     */
    @Observed(name="meeting.get")
    public Meeting getMeeting(UUID userId, UUID meetingId) {
        Calendar calendar = calendarService.findByUserId(userId);
        Meeting meeting = meetingRepository.findByIdAndCalendarId(meetingId, calendar.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + meetingId));

        return meetingRepository.findWithDetailsById(meeting.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + meetingId));
    }

    private Set<User> resolveParticipants(Set<UUID> participantIds) {
        List<User> users = userRepository.findAllById(participantIds);
        if (users.size() != participantIds.size()) {
            throw new ResourceNotFoundException("One or more participants not found");
        }
        return new HashSet<>(users);
    }


}
