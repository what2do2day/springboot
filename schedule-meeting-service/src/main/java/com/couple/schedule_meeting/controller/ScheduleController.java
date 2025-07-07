package com.couple.schedule_meeting.controller;

import com.couple.schedule_meeting.dto.ScheduleCreateRequest;
import com.couple.schedule_meeting.dto.ScheduleResponse;
import com.couple.schedule_meeting.dto.ScheduleUpdateRequest;
import com.couple.schedule_meeting.dto.ScheduleCalendarResponse;
import com.couple.schedule_meeting.entity.Schedule;
import com.couple.schedule_meeting.service.ScheduleService;
import com.couple.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;
    private static final Logger log = LoggerFactory.getLogger(ScheduleController.class);

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
    @Operation(summary = "달력 조회", description = "특정 년월의 스케줄, 미팅, 생일 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<ScheduleCalendarResponse>> getCalendar(
            @Parameter(description = "커플 ID", example = "123e4567-e89b-12d3-a456-426614174000") @RequestHeader("X-Couple-ID") String coupleId,
            @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "년도", example = "2024") @RequestParam("year") int year,
            @Parameter(description = "월", example = "12") @RequestParam("month") int month) {
        
        try {
            log.info("달력 조회 API 호출: coupleId={}, userId={}, year={}, month={}", coupleId, userId, year, month);
            
            // 입력값 검증
            if (year < 1900 || year > 2100) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("유효하지 않은 년도입니다: " + year));
            }
            if (month < 1 || month > 12) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("유효하지 않은 월입니다: " + month));
            }
            
            UUID coupleUuid = UUID.fromString(coupleId);
            ScheduleCalendarResponse response = scheduleService.getCalendar(coupleUuid, year, month, userId);
            
            return ResponseEntity.ok(ApiResponse.success("달력 조회 성공", response));
            
        } catch (IllegalArgumentException e) {
            log.error("잘못된 UUID 형식: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("잘못된 커플 ID 형식입니다: " + coupleId));
        } catch (Exception e) {
            log.error("달력 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("달력 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}