package com.bantunes82.meeting.schedule.controller.v1.mapper;

import com.bantunes82.meeting.schedule.controller.v1.dto.TimeSlotResponse;
import com.bantunes82.meeting.schedule.model.TimeSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting TimeSlot entities to DTOs.
 */
@Mapper(componentModel = "spring")
public interface TimeSlotMapper {

    @Mapping(target = "hasMeeting", expression = "java(timeSlot.getMeeting() != null)")
    TimeSlotResponse toResponse(TimeSlot timeSlot);
}
