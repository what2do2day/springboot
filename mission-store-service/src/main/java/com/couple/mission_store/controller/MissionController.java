package com.couple.mission_store.controller;

import com.couple.mission_store.dto.ApiResponse;
import com.couple.mission_store.dto.CoupleMissionResponse;
import com.couple.mission_store.dto.CouplesCoinsResponse;
import com.couple.mission_store.dto.MissionRequest;
import com.couple.mission_store.dto.MissionResponse;
import com.couple.mission_store.service.CoupleMissionService;
import com.couple.mission_store.service.CouplesCoinsService;
import com.couple.mission_store.service.MissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;
    private final CoupleMissionService coupleMissionService;
    private final CouplesCoinsService couplesCoinsService;

    // 관리자용: 미션 생성
    @PostMapping
    public ResponseEntity<ApiResponse<MissionResponse>> createMission(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId,
            @Valid @RequestBody MissionRequest request) {
        log.info("미션 생성 요청 - userId: {}, coupleId: {}, title: {}", userId, coupleId, request.getTitle());

        MissionResponse response = missionService.createMission(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("미션이 생성되었습니다.", response));
    }

    // 전체 미션 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<MissionResponse>>> getAllMissions(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId) {
        log.info("전체 미션 목록 조회 요청 - userId: {}, coupleId: {}", userId, coupleId);

        List<MissionResponse> missions = missionService.getAllMissions();
        return ResponseEntity.ok(ApiResponse.success("미션 목록 조회 성공", missions));
    }

    // 미션 상세 조회
    @GetMapping("/{missionId}")
    public ResponseEntity<ApiResponse<MissionResponse>> getMissionById(
            @PathVariable UUID missionId,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId) {
        log.info("미션 상세 조회 - userId: {}, coupleId: {}, missionId: {}", userId, coupleId, missionId);

        MissionResponse response = missionService.getMissionById(missionId);
        return ResponseEntity.ok(ApiResponse.success("미션 조회 성공", response));
    }

    // 커플별 미션 목록 조회
    @GetMapping("/couple")
    public ResponseEntity<ApiResponse<List<CoupleMissionResponse>>> getCoupleMissions(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Couple-ID") String coupleId) {
        log.info("커플 미션 목록 조회 - userId: {}, coupleId: {}", userId, coupleId);

        UUID coupleIdUUID = UUID.fromString(coupleId);
        List<CoupleMissionResponse> missions = coupleMissionService.getCoupleMissions(coupleIdUUID);
        return ResponseEntity.ok(ApiResponse.success("커플 미션 목록 조회 성공", missions));
    }

    // 오늘의 미션 조회
    @GetMapping("/couple/today")
    public ResponseEntity<ApiResponse<CoupleMissionResponse>> getTodayMission(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Couple-ID") String coupleId) {
        log.info("오늘의 미션 조회 - userId: {}, coupleId: {}", userId, coupleId);

        UUID coupleIdUUID = UUID.fromString(coupleId);
        CoupleMissionResponse mission = coupleMissionService.getTodayMission(coupleIdUUID);
        return ResponseEntity.ok(ApiResponse.success("오늘의 미션 조회 성공", mission));
    }

    // 미션 완료 처리
    @PostMapping("/couple/complete")
    public ResponseEntity<ApiResponse<CoupleMissionResponse>> completeMission(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Couple-ID") String coupleId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate assignedDate) {
        log.info("미션 완료 처리 - userId: {}, coupleId: {}, assignedDate: {}", userId, coupleId, assignedDate);

        UUID coupleIdUUID = UUID.fromString(coupleId);
        CoupleMissionResponse response = coupleMissionService.completeMission(coupleIdUUID, assignedDate);
        return ResponseEntity.ok(ApiResponse.success("미션 완료 처리 성공", response));
    }

    // 커플 코인 잔액 조회
    @GetMapping("/couple/coins")
    public ResponseEntity<ApiResponse<CouplesCoinsResponse>> getCoupleCoins(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Couple-ID") String coupleId) {
        log.info("커플 코인 잔액 조회 - userId: {}, coupleId: {}", userId, coupleId);

        UUID coupleIdUUID = UUID.fromString(coupleId);
        CouplesCoinsResponse response = couplesCoinsService.getCoupleCoins(coupleIdUUID);
        return ResponseEntity.ok(ApiResponse.success("코인 잔액 조회 성공", response));
    }
}