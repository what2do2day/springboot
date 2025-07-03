package com.couple.schedule_meeting.controller;

import com.couple.common.dto.ApiResponse;
import com.couple.schedule_meeting.dto.ScheduleRequest;
import com.couple.schedule_meeting.entity.Schedule;
import com.couple.schedule_meeting.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule", description = "일정 관련 API")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    @Operation(summary = "일정 생성", description = "새로운 일정을 생성합니다.")
    public ResponseEntity<ApiResponse<Schedule>> createSchedule(
            HttpServletRequest request,
            @Valid @RequestBody ScheduleRequest scheduleRequest) {
        String userId = request.getHeader("X-User-ID");
        Schedule schedule = scheduleService.createSchedule(Long.parseLong(userId), scheduleRequest);
        return ResponseEntity.ok(ApiResponse.success("일정이 생성되었습니다.", schedule));
    }

    @GetMapping
    @Operation(summary = "일정 목록 조회", description = "사용자의 일정 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<Schedule>>> getSchedules(HttpServletRequest request) {
        String userId = request.getHeader("X-User-ID");
        List<Schedule> schedules = scheduleService.getSchedulesByUserId(Long.parseLong(userId));
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }

    @GetMapping("/range")
    @Operation(summary = "기간별 일정 조회", description = "특정 기간의 일정을 조회합니다.")
    public ResponseEntity<ApiResponse<List<Schedule>>> getSchedulesByDateRange(
            HttpServletRequest request,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        String userId = request.getHeader("X-User-ID");
        List<Schedule> schedules = scheduleService.getSchedulesByDateRange(Long.parseLong(userId), start, end);
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }

    @GetMapping("/{scheduleId}")
    @Operation(summary = "일정 상세 조회", description = "특정 일정의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<Schedule>> getSchedule(@PathVariable Long scheduleId) {
        Schedule schedule = scheduleService.getScheduleById(scheduleId);
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

    @PutMapping("/{scheduleId}")
    @Operation(summary = "일정 수정", description = "기존 일정을 수정합니다.")
    public ResponseEntity<ApiResponse<Schedule>> updateSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleRequest scheduleRequest) {
        Schedule schedule = scheduleService.updateSchedule(scheduleId, scheduleRequest);
        return ResponseEntity.ok(ApiResponse.success("일정이 수정되었습니다.", schedule));
    }

    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "일정 삭제", description = "일정을 삭제합니다.")
    public ResponseEntity<ApiResponse<String>> deleteSchedule(@PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.ok(ApiResponse.success("일정이 삭제되었습니다.", null));
    }
}