package com.couple.schedule_meeting.controller;

import com.couple.schedule_meeting.dto.ScheduleCreateRequest;
import com.couple.schedule_meeting.dto.ScheduleResponse;
import com.couple.schedule_meeting.dto.ScheduleUpdateRequest;
import com.couple.schedule_meeting.dto.ScheduleCalendarResponse;
import com.couple.schedule_meeting.entity.Schedule;
import com.couple.schedule_meeting.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            @RequestBody ScheduleCreateRequest request,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Couple-ID") String coupleId) {

        UUID userUuid = UUID.fromString(userId);
        UUID coupleUuid = UUID.fromString(coupleId);

        Schedule createdSchedule = scheduleService.createSchedule(request, coupleUuid, userUuid);
        return ResponseEntity.ok(ScheduleResponse.from(createdSchedule));
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> getSchedule(
            @PathVariable String scheduleId,
            @RequestHeader("X-Couple-ID") String coupleId) {
        UUID scheduleUuid = UUID.fromString(scheduleId);
        UUID coupleUuid = UUID.fromString(coupleId);
        Schedule schedule = scheduleService.getScheduleById(scheduleUuid, coupleUuid);
        return ResponseEntity.ok(ScheduleResponse.from(schedule));
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable String scheduleId,
            @RequestBody ScheduleUpdateRequest request,
            @RequestHeader("X-Couple-ID") String coupleId) {
        UUID scheduleUuid = UUID.fromString(scheduleId);
        UUID coupleUuid = UUID.fromString(coupleId);
        Schedule updatedSchedule = scheduleService.updateSchedule(scheduleUuid, request, coupleUuid);
        return ResponseEntity.ok(ScheduleResponse.from(updatedSchedule));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable String scheduleId,
            @RequestHeader("X-Couple-ID") String coupleId) {
        UUID scheduleUuid = UUID.fromString(scheduleId);
        UUID coupleUuid = UUID.fromString(coupleId);
        scheduleService.deleteSchedule(scheduleUuid, coupleUuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/calendar")
    public ResponseEntity<ScheduleCalendarResponse> getCalendar(
            @RequestParam("couple-id") String coupleId,
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        ScheduleCalendarResponse response = scheduleService.getCalendar(UUID.fromString(coupleId), year, month);
        return ResponseEntity.ok(response);
    }
}