package com.bantunes82.meeting.schedule.repository;

import com.bantunes82.meeting.schedule.model.Meeting;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Meeting entity operations with keyset pagination.
 */
@Observed
public interface MeetingRepository extends JpaRepository<Meeting, UUID> {

    /**
     * Finds a meeting with its participants and time slot eagerly loaded.
     */
    @EntityGraph(attributePaths = {"participants", "timeSlot"})
    Optional<Meeting> findWithDetailsById(UUID id);

    /**
     * Finds a meeting ensuring it belongs to the specified calendar.
     */
    @Query("""
            SELECT m FROM Meeting m
            JOIN m.timeSlot ts
            WHERE m.id = :meetingId
              AND ts.calendar.id = :calendarId
            """)
    Optional<Meeting> findByIdAndCalendarId(
            @Param("meetingId") UUID meetingId,
            @Param("calendarId") UUID calendarId);
}
