package com.bantunes82.meeting.schedule.controller.v1.mapper;

import com.bantunes82.meeting.schedule.controller.v1.dto.MeetingResponse;
import com.bantunes82.meeting.schedule.model.Meeting;
import com.bantunes82.meeting.schedule.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting Meeting entities to DTOs.
 */
@Mapper(componentModel = "spring")
public interface MeetingMapper {

    @Mapping(target = "timeSlotId", source = "timeSlot.id")
    @Mapping(target = "startTime", source = "timeSlot.startTime")
    @Mapping(target = "endTime", source = "timeSlot.endTime")
    @Mapping(target = "participants", expression = "java(mapParticipants(meeting.getParticipants()))")
    MeetingResponse toResponse(Meeting meeting);

    default Set<MeetingResponse.ParticipantResponse> mapParticipants(Set<User> participants) {
        if (participants == null) {
            return null;
        }
        return participants.stream()
                .map(u -> new MeetingResponse.ParticipantResponse(u.getId(), u.getName(), u.getEmail()))
                .collect(Collectors.toSet());
    }
}
