package com.couple.question_answer.controller;

import com.couple.common.dto.ApiResponse;
import com.couple.question_answer.dto.UserTagProfileResponse;
import com.couple.question_answer.service.UserTagProfileService;

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

public class UserTagProfileController {

    private final UserTagProfileService userTagProfileService;

    @GetMapping("/my-profiles")
    public ResponseEntity<ApiResponse<List<UserTagProfileResponse>>> getMyTagProfiles(
            @RequestHeader("X-User-ID") String userId) {
        log.info("내 태그 프로필 조회: {}", userId);

        List<UserTagProfileResponse> profiles = userTagProfileService.getUserTagProfiles(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("태그 프로필 조회 성공", profiles));
    }

    @GetMapping("/my-profiles/{tagId}")
    public ResponseEntity<ApiResponse<UserTagProfileResponse>> getMyTagProfile(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable UUID tagId) {
        log.info("내 특정 태그 프로필 조회: userId={}, tagId={}", userId, tagId);

        UserTagProfileResponse profile = userTagProfileService.getUserTagProfile(UUID.fromString(userId), tagId);
        return ResponseEntity.ok(ApiResponse.success("태그 프로필 조회 성공", profile));
    }

    @DeleteMapping("/my-profiles/reset")
    public ResponseEntity<ApiResponse<String>> resetMyTagProfiles(
            @RequestHeader("X-User-ID") String userId) {
        log.info("내 태그 프로필 초기화: {}", userId);

        userTagProfileService.resetUserTagProfiles(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("태그 프로필 초기화 성공", null));
    }
}