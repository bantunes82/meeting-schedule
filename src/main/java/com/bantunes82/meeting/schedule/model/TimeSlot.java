package com.bantunes82.meeting.schedule.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a time slot in a user's calendar that can be free or busy.
 */
@Entity
@Table(name = "time_slots")
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id", nullable = false)
    private Calendar calendar;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TimeSlotStatus status = TimeSlotStatus.FREE;

    @Version
    @Column(nullable = false)
    private Integer version;

    @OneToOne(mappedBy = "timeSlot", cascade = CascadeType.ALL, orphanRemoval = true)
    private Meeting meeting;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TimeSlot() {
    }

    public TimeSlot(Calendar calendar, Instant startTime, Instant endTime) {
        this.calendar = calendar;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public TimeSlot(UUID id, Calendar calendar, Instant startTime, Instant endTime) {
        this(calendar, startTime, endTime);
        this.id = id;
    }

    public TimeSlot(UUID id, Calendar calendar, Instant startTime, Instant endTime, TimeSlotStatus status) {
        this(id, calendar, startTime, endTime);
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public TimeSlotStatus getStatus() {
        return status;
    }

    public void setStatus(TimeSlotStatus status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TimeSlot timeSlot)) return false;
        return Objects.equals(calendar, timeSlot.calendar) && Objects.equals(startTime, timeSlot.startTime) && Objects.equals(endTime, timeSlot.endTime) && status == timeSlot.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(calendar, startTime, endTime, status);
    }
}
