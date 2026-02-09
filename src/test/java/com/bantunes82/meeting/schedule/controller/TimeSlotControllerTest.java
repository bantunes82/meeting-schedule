package com.bantunes82.meeting.schedule.controller;

import com.bantunes82.meeting.schedule.controller.v1.dto.TimeSlotRequest;
import com.bantunes82.meeting.schedule.controller.v1.dto.TimeSlotResponse;
import com.bantunes82.meeting.schedule.controller.v1.dto.TimeSlotStatus;
import com.bantunes82.meeting.schedule.controller.v1.dto.UpdateTimeSlotStatusRequest;
import com.bantunes82.meeting.schedule.controller.v1.mapper.TimeSlotMapper;
import com.bantunes82.meeting.schedule.exception.ResourceNotFoundException;
import com.bantunes82.meeting.schedule.exception.TimeSlotOverlapException;
import com.bantunes82.meeting.schedule.controller.handler.GlobalExceptionHandler;
import com.bantunes82.meeting.schedule.model.Calendar;
import com.bantunes82.meeting.schedule.model.TimeSlot;
import com.bantunes82.meeting.schedule.model.User;
import com.bantunes82.meeting.schedule.service.TimeSlotService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest({ TimeSlotController.class, GlobalExceptionHandler.class })
class TimeSlotControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private TimeSlotService timeSlotService;

        @MockitoBean
        private TimeSlotMapper timeSlotMapper;

        private static final UUID USER_ID = UUID.randomUUID();
        private static final UUID SLOT_ID = UUID.randomUUID();
        private static final String BASE_URL = "/api/1.0/users/" + USER_ID + "/time-slots";
        private Calendar calendar;

        @BeforeEach
        void setUp() {
                var user = new User("Alice", "alice@example.com");
                calendar = new Calendar(user);
        }

        @Test
        void createTimeSlot_shouldReturn201() throws Exception {
                Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
                Instant end = start.plus(1, ChronoUnit.HOURS);
                var timeSlot = new TimeSlot(SLOT_ID, calendar, start, end);

                when(timeSlotService.createTimeSlot(eq(USER_ID), eq(start), eq(end))).thenReturn(timeSlot);
                when(timeSlotMapper.toResponse(any())).thenReturn(
                                new TimeSlotResponse(SLOT_ID, start, end, TimeSlotStatus.FREE, false, null, null));

                var request = new TimeSlotRequest(start, end);
                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(SLOT_ID.toString()))
                                .andExpect(jsonPath("$.status").value("FREE"));
        }

        @Test
        void createTimeSlot_withOverlap_shouldReturn409() throws Exception {
                Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
                Instant end = start.plus(1, ChronoUnit.HOURS);

                when(timeSlotService.createTimeSlot(eq(USER_ID), eq(start), eq(end)))
                                .thenThrow(new TimeSlotOverlapException("Time slot overlaps with an existing slot"));

                var request = new TimeSlotRequest(start, end);
                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message").value("Time slot overlaps with an existing slot"));
        }

        @Test
        void createTimeSlot_withInvalidTimeRange_shouldReturn400() throws Exception {
                Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
                Instant end = start.minus(1, ChronoUnit.HOURS); // Invalid: end before start

                when(timeSlotService.createTimeSlot(eq(USER_ID), eq(start), eq(end)))
                                .thenThrow(new IllegalArgumentException("End time must be after start time"));

                var request = new TimeSlotRequest(start, end);
                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("End time must be after start time"));
        }

        @Test
        void getTimeSlot_shouldReturn200() throws Exception {
                Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
                Instant end = start.plus(1, ChronoUnit.HOURS);
                var timeSlot = new TimeSlot(SLOT_ID, calendar, start, end);

                when(timeSlotService.getTimeSlot(USER_ID, SLOT_ID)).thenReturn(timeSlot);
                when(timeSlotMapper.toResponse(any())).thenReturn(
                                new TimeSlotResponse(SLOT_ID, start, end, TimeSlotStatus.FREE, false, null, null));

                mockMvc.perform(get(BASE_URL + "/" + SLOT_ID))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(SLOT_ID.toString()))
                                .andExpect(jsonPath("$.status").value("FREE"));
        }

        @Test
        void getTimeSlot_notFound_shouldReturn404() throws Exception {
                when(timeSlotService.getTimeSlot(USER_ID, SLOT_ID))
                                .thenThrow(new ResourceNotFoundException("Time slot not found"));

                mockMvc.perform(get(BASE_URL + "/" + SLOT_ID))
                                .andExpect(status().isNotFound());
        }

        @Test
        void updateTimeSlot_shouldReturn200() throws Exception {
                Instant start = Instant.now().plus(2, ChronoUnit.DAYS);
                Instant end = start.plus(1, ChronoUnit.HOURS);
                var timeSlot = new TimeSlot(SLOT_ID, calendar, start, end);

                when(timeSlotService.updateTimeSlot(eq(USER_ID), eq(SLOT_ID), eq(start), eq(end))).thenReturn(timeSlot);
                when(timeSlotMapper.toResponse(any())).thenReturn(
                                new TimeSlotResponse(SLOT_ID, start, end, TimeSlotStatus.FREE, false, null, null));

                var request = new TimeSlotRequest(start, end);
                mockMvc.perform(put(BASE_URL + "/" + SLOT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(SLOT_ID.toString()));
        }

        @Test
        void deleteTimeSlot_shouldReturn204() throws Exception {
                doNothing().when(timeSlotService).deleteTimeSlot(USER_ID, SLOT_ID);

                mockMvc.perform(delete(BASE_URL + "/" + SLOT_ID))
                                .andExpect(status().isNoContent());
        }

        @Test
        void updateTimeSlotStatus_shouldReturn200() throws Exception {
                Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
                Instant end = start.plus(1, ChronoUnit.HOURS);
                var timeSlot = new TimeSlot(SLOT_ID, calendar, start, end,
                                com.bantunes82.meeting.schedule.model.TimeSlotStatus.BUSY);

                when(timeSlotService.updateTimeSlotStatus(eq(USER_ID), eq(SLOT_ID),
                                eq(com.bantunes82.meeting.schedule.model.TimeSlotStatus.BUSY))).thenReturn(timeSlot);
                when(timeSlotMapper.toResponse(any())).thenReturn(
                                new TimeSlotResponse(SLOT_ID, start, end, TimeSlotStatus.BUSY, false, null, null));

                var request = new UpdateTimeSlotStatusRequest(TimeSlotStatus.BUSY);
                mockMvc.perform(patch(BASE_URL + "/" + SLOT_ID + "/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("BUSY"));
        }
}
