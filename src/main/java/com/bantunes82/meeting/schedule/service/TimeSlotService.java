package com.bantunes82.meeting.schedule.service;

import com.bantunes82.meeting.schedule.exception.ResourceNotFoundException;
import com.bantunes82.meeting.schedule.exception.TimeSlotOverlapException;
import com.bantunes82.meeting.schedule.model.Calendar;
import com.bantunes82.meeting.schedule.model.TimeSlot;
import com.bantunes82.meeting.schedule.model.TimeSlotStatus;
import com.bantunes82.meeting.schedule.repository.TimeSlotRepository;
import io.micrometer.observation.annotation.Observed;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for managing time slots.
 */
@Service
@Transactional(readOnly = true)
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final CalendarService calendarService;

    public TimeSlotService(TimeSlotRepository timeSlotRepository, CalendarService calendarService) {
        this.timeSlotRepository = timeSlotRepository;
        this.calendarService = calendarService;
    }

    /**
     * Creates a new time slot for the user's calendar.
     * Validates no overlap exists (app-level check + DB exclusion constraint as safety net).
     *
     * @param userId  the owner's UUID
     * @param startTime the start time of the slot
     * @param endTime   the end time of the slot
     * @return the created time slot
     */
    @Observed(name="timeslot.create")
    @Transactional
    public TimeSlot createTimeSlot(UUID userId, Instant startTime, Instant endTime) {
        validateTimeRange(startTime, endTime);
        Calendar calendar = calendarService.findByUserId(userId);

        if (timeSlotRepository.existsOverlappingSlot(calendar.getId(), startTime, endTime)) {
            throw new TimeSlotOverlapException("Time slot overlaps with an existing slot");
        }

        TimeSlot timeSlot = new TimeSlot(calendar, startTime, endTime);
        try {
            timeSlot = timeSlotRepository.save(timeSlot);
        } catch (DataIntegrityViolationException ex) {
            throw new TimeSlotOverlapException("Time slot overlaps with an existing slot");
        }
        return timeSlot;
    }


    /**
     * Gets a single time slot, validating ownership.
     *
     * @param userId the owner's UUID
     * @param slotId the time slot UUID
     * @return the time slot response
     */
    @Observed(name="timeslot.get")
    public TimeSlot getTimeSlot(UUID userId, UUID slotId) {
        Calendar calendar = calendarService.findByUserId(userId);
        return findSlotByIdAndCalendar(slotId, calendar.getId());
    }

    /**
     * Updates a time slot's start and end times, validating no overlap.
     *
     * @param userId  the owner's UUID
     * @param slotId  the time slot UUID
     * @param startTime the new start time
     * @param endTime   the new end time
     * @return the updated time slot
     */
    @Observed(name="timeslot.update")
    @Transactional
    public TimeSlot updateTimeSlot(UUID userId, UUID slotId, Instant startTime, Instant endTime) {
        validateTimeRange(startTime, endTime);
        Calendar calendar = calendarService.findByUserId(userId);
        TimeSlot timeSlot = findSlotByIdAndCalendar(slotId, calendar.getId());

        if (timeSlotRepository.existsOverlappingSlotExcluding(
                calendar.getId(), slotId, startTime, endTime)) {
            throw new TimeSlotOverlapException("Updated time slot would overlap with an existing slot");
        }

        timeSlot.setStartTime(startTime);
        timeSlot.setEndTime(endTime);
        try {
            timeSlot = timeSlotRepository.save(timeSlot);
        } catch (DataIntegrityViolationException ex) {
            throw new TimeSlotOverlapException("Updated time slot would overlap with an existing slot");
        }
        return timeSlot;
    }

    /**
     * Deletes a time slot, validating ownership.
     *
     * @param userId the owner's UUID
     * @param slotId the time slot UUID
     */
    @Observed(name="timeslot.delete")
    @Transactional
    public void deleteTimeSlot(UUID userId, UUID slotId) {
        Calendar calendar = calendarService.findByUserId(userId);
        TimeSlot timeSlot = findSlotByIdAndCalendar(slotId, calendar.getId());
        timeSlotRepository.delete(timeSlot);
    }

    /**
     * Changes a time slot's status.
     * Setting BUSY to FREE with an existing meeting cascade-deletes the meeting.
     *
     * @param userId  the owner's UUID
     * @param slotId  the time slot UUID
     * @param status the new status
     * @return the updated time slot
     */
    @Observed(name="timeslot.update-status")
    @Transactional
    public TimeSlot updateTimeSlotStatus(UUID userId, UUID slotId, TimeSlotStatus status) {
        Calendar calendar = calendarService.findByUserId(userId);
        TimeSlot timeSlot = findSlotByIdAndCalendar(slotId, calendar.getId());

        if (status == TimeSlotStatus.FREE && timeSlot.getMeeting() != null) {
            timeSlot.setMeeting(null);
        }

        timeSlot.setStatus(status);
        return timeSlotRepository.save(timeSlot);
    }

    private TimeSlot findSlotByIdAndCalendar(UUID slotId, UUID calendarId) {
        return timeSlotRepository.findByIdAndCalendarId(slotId, calendarId)
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found with id: " + slotId));
    }

    private void validateTimeRange(Instant startTime, Instant endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

}
