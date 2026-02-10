package com.bantunes82.meeting.schedule.repository;

import com.bantunes82.meeting.schedule.model.Calendar;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Calendar entity operations.
 */
@Observed
public interface CalendarRepository extends JpaRepository<Calendar, UUID> {

    /**
     * Finds a calendar by its owner's user ID.
     *
     * @param userId the user's UUID
     * @return the calendar if found
     */
    Optional<Calendar> findByUserId(UUID userId);
}
