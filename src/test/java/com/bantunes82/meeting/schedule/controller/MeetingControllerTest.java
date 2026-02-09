package com.bantunes82.meeting.schedule.controller;

import com.bantunes82.meeting.schedule.controller.v1.dto.MeetingRequest;
import com.bantunes82.meeting.schedule.controller.v1.dto.MeetingResponse;
import com.bantunes82.meeting.schedule.exception.ResourceNotFoundException;
import com.bantunes82.meeting.schedule.exception.TimeSlotNotAvailableException;
import com.bantunes82.meeting.schedule.controller.handler.GlobalExceptionHandler;
import com.bantunes82.meeting.schedule.controller.v1.mapper.MeetingMapper;
import com.bantunes82.meeting.schedule.model.Calendar;
import com.bantunes82.meeting.schedule.model.Meeting;
import com.bantunes82.meeting.schedule.model.TimeSlot;
import com.bantunes82.meeting.schedule.model.User;
import com.bantunes82.meeting.schedule.service.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({MeetingController.class, GlobalExceptionHandler.class})
class MeetingControllerTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID SLOT_ID = UUID.randomUUID();
    private static final UUID MEETING_ID = UUID.randomUUID();
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private MeetingService meetingService;
    @MockitoBean
    private MeetingMapper meetingMapper;

    private MeetingRequest request;
    private Meeting meeting;
    private Instant startTime;
    private Instant endTime;
    private MeetingResponse response;

    @BeforeEach
    void setUp() {
        var user = new User("Alice", "alice@example.com");
        var calendar = new Calendar(user);
        User participant = new User("Bob", "bob@example.com");
        request = new MeetingRequest("Stand-up", "Daily sync", Set.of(UUID.randomUUID()));
        var timeSlot = new TimeSlot(calendar, startTime, endTime);
        meeting = new Meeting(MEETING_ID, timeSlot, "Stand-up", "Daily sync", Set.of(participant));
        startTime = Instant.now().plus(1, ChronoUnit.DAYS);
        endTime = startTime.plus(1, ChronoUnit.HOURS);
        response = new MeetingResponse(MEETING_ID, SLOT_ID, startTime, endTime, "Stand-up", "Daily sync", Set.of(), Instant.now(), Instant.now());
    }

    @Test
    void createMeeting_shouldReturn201() throws Exception {
        when(meetingService.createMeeting(eq(USER_ID), eq(SLOT_ID), any(), any(), any())).thenReturn(meeting);
        when(meetingMapper.toResponse(any())).thenReturn(response);

        mockMvc.perform(post("/api/1.0/users/" + USER_ID + "/time-slots/" + SLOT_ID + "/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(MEETING_ID.toString()))
                .andExpect(jsonPath("$.title").value("Stand-up"));
    }

    @Test
    void createMeeting_slotNotAvailable_shouldReturn409() throws Exception {
        when(meetingService.createMeeting(eq(USER_ID), eq(SLOT_ID), any(), any(), any()))
                .thenThrow(new TimeSlotNotAvailableException("Time slot is not available for booking"));

        mockMvc.perform(post("/api/1.0/users/" + USER_ID + "/time-slots/" + SLOT_ID + "/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Time slot is not available for booking"));
    }

    @Test
    void createMeeting_invalidRequest_shouldReturn400() throws Exception {
        var invalidRequest = new MeetingRequest("", "", Set.of());
        mockMvc.perform(post("/api/1.0/users/" + USER_ID + "/time-slots/" + SLOT_ID + "/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasItems(
                        "title: Title is required",
                        "participantIds: At least one participant is required"
                )));
    }

    @Test
    void createMeeting_nonExistentSlot_shouldReturn404() throws Exception {
        when(meetingService.createMeeting(eq(USER_ID), eq(SLOT_ID), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Time slot not found with id: " + SLOT_ID));

        mockMvc.perform(post("/api/1.0/users/" + USER_ID + "/time-slots/" + SLOT_ID + "/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Time slot not found with id: " + SLOT_ID));
    }

    @Test
    void getMeeting_shouldReturn200() throws Exception {
        when(meetingService.getMeeting(USER_ID, MEETING_ID)).thenReturn(meeting);
        when(meetingMapper.toResponse(any())).thenReturn(response);

        mockMvc.perform(get("/api/1.0/users/" + USER_ID + "/meetings/" + MEETING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Stand-up"));
    }

    @Test
    void getMeeting_notFound_shouldReturn404() throws Exception {
        when(meetingService.getMeeting(USER_ID, MEETING_ID)).thenThrow(new ResourceNotFoundException("Meeting not found with id: " + MEETING_ID));

        mockMvc.perform(get("/api/1.0/users/" + USER_ID + "/meetings/" + MEETING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Meeting not found with id: " + MEETING_ID));
    }
}
