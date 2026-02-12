package com.bantunes82.meeting.schedule.service;

import com.bantunes82.meeting.schedule.exception.ResourceNotFoundException;
import com.bantunes82.meeting.schedule.model.Calendar;
import com.bantunes82.meeting.schedule.repository.CalendarRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for calendar.
 */
@Service
@Transactional(readOnly = true)
public class CalendarService {

    private final CalendarRepository calendarRepository;

    public CalendarService(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    /**
     * Finds a calendar by its owner's user ID, with caching.
     *
     * @param userId the user's UUID
     * @return the calendar
     * @throws ResourceNotFoundException if no calendar is found for the user
     */
    @Cacheable(value = "calendars", key = "#userId")
    public Calendar findByUserId(UUID userId) {
        return calendarRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Calendar not found to the User with id: " + userId));
    }
}
