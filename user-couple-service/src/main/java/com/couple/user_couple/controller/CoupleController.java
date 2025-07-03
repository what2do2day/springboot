package com.couple.user_couple.controller;

import com.couple.common.dto.ApiResponse;
import com.couple.user_couple.dto.CoupleDateRequest;
import com.couple.user_couple.dto.CoupleMatchRequest;
import com.couple.user_couple.dto.CoupleMatchAcceptRequest;
import com.couple.user_couple.dto.CoupleResponse;
import com.couple.user_couple.dto.HomeInfoResponse;
import com.couple.user_couple.service.CoupleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/couples")
@RequiredArgsConstructor
@Tag(name = "Couple", description = "커플 관련 API")
public class CoupleController {

    private final CoupleService coupleService;

    @PostMapping("/match")
    @Operation(summary = "커플 매칭 코드 생성", description = "커플 매칭을 위한 코드를 생성합니다.")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateMatchCode(
            @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody CoupleMatchRequest request) {

        String matchCode = coupleService.generateMatchCode(UUID.fromString(userId), request);

        Map<String, String> response = new HashMap<>();
        response.put("matchCode", matchCode);

        return ResponseEntity.ok(ApiResponse.success("매칭 코드가 생성되었습니다.", response));
    }

    @PostMapping("/match/accept")
    @Operation(summary = "커플 매칭 수락", description = "매칭 코드를 입력하여 커플을 생성합니다.")
    public ResponseEntity<ApiResponse<String>> acceptMatchCode(
            @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody CoupleMatchAcceptRequest request) {

        coupleService.acceptMatchCode(UUID.fromString(userId), request);
        return ResponseEntity.ok(ApiResponse.success("커플 매칭이 완료되었습니다.", null));
    }

    @DeleteMapping("/break")
    @Operation(summary = "커플 파기", description = "현재 커플을 해제합니다.")
    public ResponseEntity<ApiResponse<String>> breakCouple(
            @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") @RequestHeader("X-User-ID") String userId) {

        coupleService.breakCouple(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("커플이 해제되었습니다.", null));
    }

    @GetMapping("/home")
    @Operation(summary = "홈 정보 조회", description = "홈 화면에 필요한 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<HomeInfoResponse>> getHomeInfo(
            @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") @RequestHeader("X-User-ID") String userId) {

        HomeInfoResponse response = coupleService.getHomeInfo(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("홈 정보 조회 성공", response));
    }

    @GetMapping("/me")
    @Operation(summary = "커플 정보 조회", description = "현재 커플의 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<CoupleResponse>> getCoupleInfo(
            @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") @RequestHeader("X-User-ID") String userId) {

        CoupleResponse response = coupleService.getCoupleInfo(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("커플 정보 조회 성공", response));
    }

    @PostMapping("/date")
    @Operation(summary = "커플 날짜 설정", description = "커플의 날짜를 설정합니다.")
    public ResponseEntity<ApiResponse<String>> setCoupleDate(
            @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody CoupleDateRequest request) {

        coupleService.setCoupleDate(UUID.fromString(userId), request);
        return ResponseEntity.ok(ApiResponse.success("커플 날짜가 설정되었습니다.", null));
    }
}