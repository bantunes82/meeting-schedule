package com.bantunes82.meeting.schedule.repository;

import com.bantunes82.meeting.schedule.model.TimeSlot;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for TimeSlot entity.
 */
@Observed
public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {


    /**
     * Finds a time slot ensuring it belongs to the specified calendar.
     */
    Optional<TimeSlot> findByIdAndCalendarId(UUID id, UUID calendarId);
}
