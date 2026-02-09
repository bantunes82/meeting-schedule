package com.bantunes82.meeting.schedule.exception;

/**
 * Thrown when attempting to create a meeting on a time slot that is not FREE.
 */
public class TimeSlotNotAvailableException extends RuntimeException {

    public TimeSlotNotAvailableException(String message) {
        super(message);
    }
}
