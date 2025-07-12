package com.couple.user_couple.controller;

import com.couple.common.dto.ApiResponse;
import com.couple.user_couple.dto.CoupleDateRequest;
import com.couple.user_couple.dto.CoupleMatchRequest;
import com.couple.user_couple.dto.CoupleMatchAcceptRequest;
import com.couple.user_couple.dto.CoupleResponse;
import com.couple.user_couple.dto.HomeInfoResponse;
import com.couple.user_couple.dto.CoupleMemberResponse;
import com.couple.user_couple.dto.UserResponse;
import com.couple.user_couple.dto.CoupleInfoResponse;
import com.couple.user_couple.dto.CoupleRankResponse;
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
import java.util.List;
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
    @Operation(summary = "커플 매칭 코드 생성", description = "커플 매칭을 위한 코드를 생성하고 새로운 토큰을 반환합니다.")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateMatchCode(
            @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody CoupleMatchRequest request) {

        Map<String, String> response = coupleService.generateMatchCode(UUID.fromString(userId), request);

        log.info("매칭 코드 생성 완료 - 매칭 코드: {}, 새로운 토큰: {}",
                response.get("matchCode"), response.get("newToken"));

        return ResponseEntity.ok(ApiResponse.success("매칭 코드가 생성되었습니다.", response));
    }

    @PostMapping("/match/accept")
    @Operation(summary = "커플 매칭 수락", description = "매칭 코드를 입력하여 커플을 생성하고 새로운 토큰을 반환합니다.")
    public ResponseEntity<ApiResponse<Map<String, String>>> acceptMatchCode(
            @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody CoupleMatchAcceptRequest request) {

        Map<String, String> response = coupleService.acceptMatchCode(UUID.fromString(userId), request);

        log.info("커플 매칭 완료 - 새로운 토큰: {}", response.get("newToken"));

        return ResponseEntity.ok(ApiResponse.success("커플 매칭이 완료되었습니다.", response));
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

    @GetMapping("/info")
    @Operation(summary = "커플 정보 조회", description = "사용자 ID로 커플 정보와 디데이를 조회합니다.")
    public ResponseEntity<ApiResponse<CoupleInfoResponse>> getCoupleInfoByUserId(
            @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") @RequestHeader("X-User-ID") String userId) {
        
        log.info("커플 정보 조회 API 호출: {}", userId);
        CoupleInfoResponse response = coupleService.getCoupleInfoByUserId(UUID.fromString(userId));
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

    @GetMapping("/members")
    @Operation(summary = "커플 멤버 정보 조회", description = "해당 유저가 속한 커플의 모든 멤버 정보를 조회합니다.")
    public ResponseEntity<List<CoupleMemberResponse>> getCoupleMembers(
            @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") @RequestHeader("X-User-ID") String userId) {
        log.info("커플 멤버 정보 조회 API 호출: {}", userId);
        List<CoupleMemberResponse> members = coupleService.getCoupleMembers(UUID.fromString(userId));
        return ResponseEntity.ok(members);
    }

    @GetMapping("/dday")
    @Operation(summary = "커플 디데이 조회", description = "커플의 시작일부터 현재까지의 일수를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDday(
            @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") @RequestHeader("X-User-ID") String userId) {
        
        log.info("커플 디데이 조회 API 호출: {}", userId);
        long dday = coupleService.calculateDdayByUserId(UUID.fromString(userId));
        
        Map<String, Object> response = new HashMap<>();
        response.put("dday", dday);
        response.put("message", dday + "일째");
        
        return ResponseEntity.ok(ApiResponse.success("디데이 조회 성공", response));
    }

    @GetMapping("/rank")
    @Operation(summary = "커플 랭킹 조회", description = "전체 커플의 점수 기준 랭킹을 조회합니다.")
    public ResponseEntity<ApiResponse<List<CoupleRankResponse>>> getCoupleRanks() {
        log.info("커플 랭킹 조회 API 호출");
        List<CoupleRankResponse> ranks = coupleService.getCoupleRanks();
        return ResponseEntity.ok(ApiResponse.success("커플 랭킹 조회 성공", ranks));
    }
}