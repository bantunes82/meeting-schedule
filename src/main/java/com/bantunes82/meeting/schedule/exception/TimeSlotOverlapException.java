package com.bantunes82.meeting.schedule.exception;

/**
 * Thrown when a time slot overlaps with an existing slot in the same calendar.
 */
public class TimeSlotOverlapException extends RuntimeException {

    public TimeSlotOverlapException(String message) {
        super(message);
    }
}
