package com.bantunes82.meeting.schedule.service;

import com.bantunes82.meeting.schedule.exception.ResourceNotFoundException;
import com.bantunes82.meeting.schedule.exception.TimeSlotOverlapException;
import com.bantunes82.meeting.schedule.model.Calendar;
import com.bantunes82.meeting.schedule.model.Meeting;
import com.bantunes82.meeting.schedule.model.TimeSlot;
import com.bantunes82.meeting.schedule.model.TimeSlotStatus;
import com.bantunes82.meeting.schedule.model.User;
import com.bantunes82.meeting.schedule.repository.CalendarRepository;
import com.bantunes82.meeting.schedule.repository.TimeSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeSlotServiceTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private CalendarRepository calendarRepository;

    @InjectMocks
    private TimeSlotService timeSlotService;

    private UUID userId;
    private UUID slotId;
    private Calendar calendar;
    private Instant startTime;
    private Instant endTime;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        slotId = UUID.randomUUID();
        var user = new User("Alice", "alice@example.com");
        calendar = new Calendar(user);
        startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        endTime = startTime.plus(1, ChronoUnit.HOURS);
    }

    @Test
    void createTimeSlot_shouldCreateSuccessfully() {
        var timeSlotExpected = new TimeSlot(calendar, startTime, endTime);

        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.existsOverlappingSlot(any(), eq(startTime), eq(endTime))).thenReturn(false);
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(timeSlotExpected);

        var timeSlotResult = timeSlotService.createTimeSlot(userId, startTime, endTime);

        assertThat(timeSlotResult).isEqualTo(timeSlotExpected);
        verify(timeSlotRepository).save(any(TimeSlot.class));
    }

    @Test
    void createTimeSlot_shouldThrowOnOverlap() {
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.existsOverlappingSlot(any(), eq(startTime), eq(endTime))).thenReturn(true);

        assertThatThrownBy(() -> timeSlotService.createTimeSlot(userId, startTime, endTime))
                .isInstanceOf(TimeSlotOverlapException.class)
                .hasMessage("Time slot overlaps with an existing slot");
    }

    @Test
    void createTimeSlot_shouldThrowWhenEndBeforeStart() {
        assertThatThrownBy(() -> timeSlotService.createTimeSlot(userId, endTime, startTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End time must be after start time");
    }

    @Test
    void createTimeSlot_shouldThrowWhenUserNotFound() {
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeSlotService.createTimeSlot(userId, startTime, endTime))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Calendar not found to the User with id: " + userId);
    }

    @Test
    void getTimeSlot_shouldReturnSlot() {
        var timeSlotExpected = new TimeSlot(calendar, startTime, endTime);

        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.findByIdAndCalendarId(slotId, calendar.getId())).thenReturn(Optional.of(timeSlotExpected));

        var timeSlotResult = timeSlotService.getTimeSlot(userId, slotId);

        assertThat(timeSlotResult).isEqualTo(timeSlotExpected);
    }

    @Test
    void getTimeSlot_shouldThrowWhenNotFound() {
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.findByIdAndCalendarId(slotId, calendar.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeSlotService.getTimeSlot(userId, slotId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Time slot not found with id: " + slotId);
    }


    @Test
    void updateTimeSlot_shouldUpdateTimes() {
        var timeSlot = new TimeSlot(calendar, startTime, endTime);
        Instant newStart = startTime.plus(2, ChronoUnit.HOURS);
        Instant newEnd = newStart.plus(1, ChronoUnit.HOURS);

        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.findByIdAndCalendarId(slotId, calendar.getId())).thenReturn(Optional.of(timeSlot));
        when(timeSlotRepository.existsOverlappingSlotExcluding(any(), eq(slotId), eq(newStart), eq(newEnd))).thenReturn(false);
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(timeSlot);

        var timeSlotResult = timeSlotService.updateTimeSlot(userId, slotId, newStart, newEnd);

        assertThat(timeSlotResult).isNotNull();
        assertThat(timeSlotResult.getStartTime()).isEqualTo(newStart);
        assertThat(timeSlotResult.getEndTime()).isEqualTo(newEnd);
        verify(timeSlotRepository).save(any(TimeSlot.class));
    }

    @Test
    void deleteTimeSlot_shouldDelete() {
        var timeSlot = new TimeSlot(calendar, startTime, endTime);

        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.findByIdAndCalendarId(slotId, calendar.getId())).thenReturn(Optional.of(timeSlot));

        timeSlotService.deleteTimeSlot(userId, slotId);

        verify(timeSlotRepository).delete(timeSlot);
    }

    @Test
    void updateTimeSlotStatus_toBusy_shouldUpdate() {
        var timeSlot = new TimeSlot(calendar, startTime, endTime);

        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.findByIdAndCalendarId(slotId, calendar.getId())).thenReturn(Optional.of(timeSlot));
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(timeSlot);

        var timeSlotResult = timeSlotService.updateTimeSlotStatus(userId, slotId, TimeSlotStatus.BUSY);

        assertThat(timeSlotResult).isNotNull();
        assertThat(timeSlotResult.getStatus()).isEqualTo(TimeSlotStatus.BUSY);
        verify(timeSlotRepository).save(any(TimeSlot.class));
    }

    @Test
    void updateTimeSlotStatus_toFreeWithMeeting_shouldRemoveMeeting() {
        var timeSlot = new TimeSlot(calendar, startTime, endTime);
        timeSlot.setStatus(TimeSlotStatus.BUSY);
        var meeting = new Meeting(timeSlot, "Test Meeting", "Desc", Set.of());
        timeSlot.setMeeting(meeting);

        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.findByIdAndCalendarId(slotId, calendar.getId())).thenReturn(Optional.of(timeSlot));
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(timeSlot);

        var timeSlotResult = timeSlotService.updateTimeSlotStatus(userId, slotId, TimeSlotStatus.FREE);

        assertThat(timeSlotResult).isNotNull();
        assertThat(timeSlotResult.getStatus()).isEqualTo(TimeSlotStatus.FREE);
        assertThat(timeSlotResult.getMeeting()).isNull();
        verify(timeSlotRepository).save(any(TimeSlot.class));
    }
}
