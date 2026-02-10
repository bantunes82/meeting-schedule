package com.bantunes82.meeting.schedule.repository;

import com.bantunes82.meeting.schedule.model.TimeSlot;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for TimeSlot entity.
 */
@Observed
public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

    /**
     * Checks if an overlapping slot exists in the same calendar.
     * Two slots overlap when: existingStart < newEnd AND existingEnd > newStart.
     * This is an application-level check; the DB exclusion constraint is the ultimate safety net.
     *
     * @param calendarId the calendar's UUID
     * @param startTime  the proposed slot start
     * @param endTime    the proposed slot end
     * @return true if an overlap exists
     */
    @Query("""
            SELECT COUNT(ts) > 0 FROM TimeSlot ts
            WHERE ts.calendar.id = :calendarId
              AND ts.startTime < :endTime
              AND ts.endTime > :startTime
            """)
    boolean existsOverlappingSlot(
            @Param("calendarId") UUID calendarId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    /**
     * Checks overlap excluding a specific slot (for update operations).
     */
    @Query("""
            SELECT COUNT(ts) > 0 FROM TimeSlot ts
            WHERE ts.calendar.id = :calendarId
              AND ts.id != :excludeId
              AND ts.startTime < :endTime
              AND ts.endTime > :startTime
            """)
    boolean existsOverlappingSlotExcluding(
            @Param("calendarId") UUID calendarId,
            @Param("excludeId") UUID excludeId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);


    /**
     * Finds a time slot ensuring it belongs to the specified calendar.
     */
    Optional<TimeSlot> findByIdAndCalendarId(UUID id, UUID calendarId);
}
