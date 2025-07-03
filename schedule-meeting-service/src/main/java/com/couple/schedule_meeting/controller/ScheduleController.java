package com.couple.schedule_meeting.controller;

import com.couple.schedule_meeting.dto.ScheduleCreateRequest;
import com.couple.schedule_meeting.entity.Schedule;
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
    public ResponseEntity<Schedule> createSchedule(
            @RequestBody ScheduleCreateRequest request,
            @RequestHeader("user_id") String userId,
            @RequestHeader("couple_id") String coupleId) {
        
        UUID userUuid = UUID.fromString(userId);
        UUID coupleUuid = UUID.fromString(coupleId);
        
        Schedule createdSchedule = scheduleService.createSchedule(request, coupleUuid, userUuid);
        return ResponseEntity.ok(createdSchedule);
    }
} 