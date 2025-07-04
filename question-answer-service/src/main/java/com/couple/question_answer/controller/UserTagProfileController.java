package com.couple.question_answer.controller;

import com.couple.common.dto.ApiResponse;
import com.couple.question_answer.dto.UserTagProfileResponse;
import com.couple.question_answer.service.UserTagProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/user-tag-profiles")
@RequiredArgsConstructor
@Tag(name = "UserTagProfile", description = "사용자 태그 프로필 관련 API")
public class UserTagProfileController {

    private final UserTagProfileService userTagProfileService;

    @GetMapping("/my-profiles")
    @Operation(summary = "내 태그 프로필 조회", description = "현재 사용자의 모든 태그 프로필을 조회합니다.")
    public ResponseEntity<ApiResponse<List<UserTagProfileResponse>>> getMyTagProfiles(
            @Parameter(description = "사용자 ID") @RequestHeader("X-User-ID") String userId) {
        log.info("내 태그 프로필 조회: {}", userId);

        List<UserTagProfileResponse> profiles = userTagProfileService.getUserTagProfiles(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("태그 프로필 조회 성공", profiles));
    }

    @GetMapping("/my-profiles/{tagId}")
    @Operation(summary = "내 특정 태그 프로필 조회", description = "현재 사용자의 특정 태그 프로필을 조회합니다.")
    public ResponseEntity<ApiResponse<UserTagProfileResponse>> getMyTagProfile(
            @Parameter(description = "사용자 ID") @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "태그 ID") @PathVariable UUID tagId) {
        log.info("내 특정 태그 프로필 조회: userId={}, tagId={}", userId, tagId);

        UserTagProfileResponse profile = userTagProfileService.getUserTagProfile(UUID.fromString(userId), tagId);
        return ResponseEntity.ok(ApiResponse.success("태그 프로필 조회 성공", profile));
    }

    @DeleteMapping("/my-profiles/reset")
    @Operation(summary = "내 태그 프로필 초기화", description = "현재 사용자의 모든 태그 프로필을 초기화합니다.")
    public ResponseEntity<ApiResponse<String>> resetMyTagProfiles(
            @Parameter(description = "사용자 ID") @RequestHeader("X-User-ID") String userId) {
        log.info("내 태그 프로필 초기화: {}", userId);

        userTagProfileService.resetUserTagProfiles(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("태그 프로필 초기화 성공", null));
    }
}