package com.couple.schedule_meeting.controller;

import com.couple.schedule_meeting.dto.ScheduleCreateRequest;
import com.couple.schedule_meeting.dto.ScheduleResponse;
import com.couple.schedule_meeting.dto.ScheduleUpdateRequest;
import com.couple.schedule_meeting.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            @RequestBody ScheduleCreateRequest request,
            @RequestHeader("user_id") String userId,
            @RequestHeader("couple_id") String coupleId) {
        
        UUID userUuid = UUID.fromString(userId);
        UUID coupleUuid = UUID.fromString(coupleId);
        
        ScheduleResponse createdSchedule = scheduleService.createSchedule(request, coupleUuid, userUuid);
        return ResponseEntity.ok(createdSchedule);
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> getSchedule(@PathVariable String scheduleId) {
        UUID scheduleUuid = UUID.fromString(scheduleId);
        ScheduleResponse schedule = scheduleService.getScheduleById(scheduleUuid);
        return ResponseEntity.ok(schedule);
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable String scheduleId,
            @RequestBody ScheduleUpdateRequest request) {
        UUID scheduleUuid = UUID.fromString(scheduleId);
        ScheduleResponse updatedSchedule = scheduleService.updateSchedule(scheduleUuid, request);
        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable String scheduleId) {
        UUID scheduleUuid = UUID.fromString(scheduleId);
        scheduleService.deleteSchedule(scheduleUuid);
        return ResponseEntity.noContent().build();
    }
} 