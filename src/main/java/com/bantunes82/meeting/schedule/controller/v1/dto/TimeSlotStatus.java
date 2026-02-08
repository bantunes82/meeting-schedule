package com.bantunes82.meeting.schedule.controller.v1.dto;

/**
 * DTO enum representing the availability status of a time slot.
 * This is used in API requests and responses to avoid exposing the model class.
 */
public enum TimeSlotStatus {
    FREE,
    BUSY
}
