package com.bantunes82.meeting.schedule.service;

import com.bantunes82.meeting.schedule.exception.ResourceNotFoundException;
import com.bantunes82.meeting.schedule.exception.TimeSlotNotAvailableException;
import com.bantunes82.meeting.schedule.model.Calendar;
import com.bantunes82.meeting.schedule.model.Meeting;
import com.bantunes82.meeting.schedule.model.TimeSlot;
import com.bantunes82.meeting.schedule.model.TimeSlotStatus;
import com.bantunes82.meeting.schedule.model.User;
import com.bantunes82.meeting.schedule.repository.CalendarRepository;
import com.bantunes82.meeting.schedule.repository.MeetingRepository;
import com.bantunes82.meeting.schedule.repository.TimeSlotRepository;
import com.bantunes82.meeting.schedule.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private CalendarRepository calendarRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MeetingService meetingService;

    private UUID userId;
    private UUID slotId;
    private UUID meetingId;
    private Calendar calendar;
    private User participant;
    private TimeSlot timeSlot;
    private String title;
    private String description;
    private Set<UUID> participantIds;
    private Meeting meeting;


    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        slotId = UUID.randomUUID();
        meetingId = UUID.randomUUID();
        var user = new User("Alice", "alice@example.com");
        participant = new User("Bob", "bob@example.com");
        calendar = new Calendar(user);
        var startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        var endTime = startTime.plus(1, ChronoUnit.HOURS);
        timeSlot = new TimeSlot(calendar, startTime, endTime);
        var participantId = UUID.randomUUID();
        title = "Stand-up";
        description = "Daily sync";
        participantIds = Set.of(participantId);

        meeting = new Meeting(timeSlot, title, description, Set.of(participant));
    }

    @Test
    void createMeeting_shouldConvertFreeSlotToMeeting() {
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.findByIdAndCalendarId(slotId, calendar.getId())).thenReturn(Optional.of(timeSlot));
        when(userRepository.findAllById(participantIds)).thenReturn(List.of(participant));
        when(meetingRepository.save(any(Meeting.class))).thenReturn(meeting);
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(timeSlot);

        var meetingResult = meetingService.createMeeting(userId, slotId, title, description, participantIds);

        assertThat(meetingResult).isEqualTo(meeting);
        assertThat(meetingResult.getTimeSlot().getStatus()).isEqualTo(TimeSlotStatus.BUSY);
        verify(meetingRepository).save(any(Meeting.class));
        verify(timeSlotRepository).save(any(TimeSlot.class));
    }

    @Test
    void createMeeting_shouldThrowWhenSlotIsBusy() {
        timeSlot.setStatus(TimeSlotStatus.BUSY);

        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.findByIdAndCalendarId(slotId, calendar.getId())).thenReturn(Optional.of(timeSlot));

        assertThatThrownBy(
                () -> meetingService.createMeeting(userId, slotId, title, description, participantIds))
                .isInstanceOf(TimeSlotNotAvailableException.class)
                .hasMessage("Time slot is not available for booking");
    }

    @Test
    void createMeeting_shouldThrowWhenSlotHasMeeting() {
        timeSlot.setMeeting(meeting);

        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.findByIdAndCalendarId(slotId, calendar.getId())).thenReturn(Optional.of(timeSlot));

        assertThatThrownBy(
                () -> meetingService.createMeeting(userId, slotId, title, description, participantIds))
                .isInstanceOf(TimeSlotNotAvailableException.class)
                .hasMessage("Time slot already has a meeting");
    }

    @Test
    void createMeeting_shouldThrowWhenSlotNotFound() {
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.findByIdAndCalendarId(slotId, calendar.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> meetingService.createMeeting(userId, slotId, title, description, participantIds))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Time slot not found with id: " + slotId);
    }

    @Test
    void createMeeting_shouldThrowWhenParticipantNotFound() {
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.findByIdAndCalendarId(slotId, calendar.getId())).thenReturn(Optional.of(timeSlot));
        when(userRepository.findAllById(participantIds)).thenReturn(List.of());

        assertThatThrownBy(() -> meetingService.createMeeting(userId, slotId, title, description, participantIds))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("One or more participants not found");
    }

    @Test
    void createMeeting_shouldThrowWhenCalendarNotFound() {
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.createMeeting(userId, slotId, title, description, participantIds))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Calendar not found to the User with id: " + userId);
    }

    @Test
    void getMeeting_shouldReturnDetails() {
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(meetingRepository.findByIdAndCalendarId(meetingId, calendar.getId())).thenReturn(Optional.of(meeting));
        when(meetingRepository.findWithDetailsById(meeting.getId())).thenReturn(Optional.of(meeting));

        Meeting meetingResult = meetingService.getMeeting(userId, meetingId);

        assertThat(meetingResult).isEqualTo(meeting);
        assertThat(meetingResult.getParticipants()).hasSize(1);
    }

    @Test
    void getMeeting_shouldThrowWhenNotFound() {
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(meetingRepository.findByIdAndCalendarId(meetingId, calendar.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.getMeeting(userId, meetingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Meeting not found with id: " + meetingId);
    }

    @Test
    void getMeeting_shouldThrowWhenWhenCalendarNotFound() {
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.getMeeting(userId, meetingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Calendar not found to the User with id: " + userId);
    }
}
