package com.couple.question_answer.controller;

import com.couple.common.dto.ApiResponse;
import com.couple.question_answer.dto.UserVectorRequest;
import com.couple.question_answer.dto.UserVectorResponse;
import com.couple.question_answer.service.UserVectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/user-vectors")
@RequiredArgsConstructor
public class UserVectorController {

    private final UserVectorService userVectorService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserVectorResponse>> createUserVector(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId) {
        log.info("사용자 벡터 생성 요청 - userId: {}, coupleId: {}", userId, coupleId);

        UserVectorResponse response = userVectorService.createUserVector(UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("사용자 벡터가 생성되었습니다.", response));
    }

    @GetMapping("/my-vector")
    public ResponseEntity<ApiResponse<UserVectorResponse>> getMyVector(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId) {
        log.info("내 벡터 조회 요청 - userId: {}, coupleId: {}", userId, coupleId);

        UserVectorResponse response = userVectorService.getUserVector(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("벡터 조회 성공", response));
    }

    @PutMapping("/my-vector")
    public ResponseEntity<ApiResponse<UserVectorResponse>> updateMyVector(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId,
            @Valid @RequestBody UserVectorRequest request) {
        log.info("내 벡터 업데이트 요청 - userId: {}, coupleId: {}", userId, coupleId);

        UserVectorResponse response = userVectorService.updateUserVector(UUID.fromString(userId), request);
        return ResponseEntity.ok(ApiResponse.success("벡터 업데이트 성공", response));
    }

    @PutMapping("/my-vector/{vectorKey}")
    public ResponseEntity<ApiResponse<UserVectorResponse>> updateSpecificVector(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId,
            @PathVariable String vectorKey,
            @RequestParam Double value) {
        log.info("특정 벡터 업데이트 요청 - userId: {}, coupleId: {}, vectorKey: {}, value: {}",
                userId, coupleId, vectorKey, value);

        UserVectorResponse response = userVectorService.updateSpecificVector(UUID.fromString(userId), vectorKey, value);
        return ResponseEntity.ok(ApiResponse.success("특정 벡터 업데이트 성공", response));
    }

    @DeleteMapping("/my-vector")
    public ResponseEntity<ApiResponse<String>> deleteMyVector(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId) {
        log.info("내 벡터 삭제 요청 - userId: {}, coupleId: {}", userId, coupleId);

        userVectorService.deleteUserVector(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("벡터 삭제 성공", null));
    }

    // 내부 서버 간 통신용 API - 헤더 인증 없이 userId로 조회
    @GetMapping("/internal/{userId}")
    public ResponseEntity<ApiResponse<UserVectorResponse>> getUserVectorByUserId(
            @PathVariable String userId) {
        log.info("내부 서버 간 벡터 조회 요청 - userId: {}", userId);

        try {
            UserVectorResponse response = userVectorService.getUserVector(UUID.fromString(userId));
            return ResponseEntity.ok(ApiResponse.success("벡터 조회 성공", response));
        } catch (IllegalArgumentException e) {
            log.warn("벡터를 찾을 수 없음 - userId: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("벡터를 찾을 수 없습니다: " + userId));
        } catch (Exception e) {
            log.error("벡터 조회 중 오류 발생 - userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("벡터 조회 중 오류가 발생했습니다"));
        }
    }
}