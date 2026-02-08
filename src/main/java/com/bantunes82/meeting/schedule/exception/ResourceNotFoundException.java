package com.bantunes82.meeting.schedule.exception;

/**
 * Thrown when a requested resource (user, time slot, meeting) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
