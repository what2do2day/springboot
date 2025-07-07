package com.couple.question_answer.controller;

import com.couple.common.dto.ApiResponse;
import com.couple.question_answer.dto.UserAnswerRequest;
import com.couple.question_answer.dto.UserAnswerResponse;
import com.couple.question_answer.service.UserAnswerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/user-answers")
@RequiredArgsConstructor
public class UserAnswerController {

    private final UserAnswerService userAnswerService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserAnswerResponse>> submitAnswer(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId,
            @Valid @RequestBody UserAnswerRequest request) {
        log.info("답변 제출 요청 - userId: {}, coupleId: {}, questionId: {}, selectedChoice: {}",
                userId, coupleId, request.getQuestionId(), request.getSelectedChoice());

        UUID userIdUUID = UUID.fromString(userId);
        UUID coupleIdUUID = coupleId != null && !"null".equals(coupleId) ? UUID.fromString(coupleId) : null;
        UserAnswerResponse response = userAnswerService.submitAnswer(userIdUUID, coupleIdUUID, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("답변이 제출되었습니다.", response));
    }

    @GetMapping("/my-answers")
    public ResponseEntity<ApiResponse<List<UserAnswerResponse>>> getMyAnswers(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId) {
        log.info("내 답변 조회 - userId: {}, coupleId: {}", userId, coupleId);

        UUID userIdUUID = UUID.fromString(userId);
        List<UserAnswerResponse> answers = userAnswerService.getUserAnswers(userIdUUID);
        return ResponseEntity.ok(ApiResponse.success("답변 목록 조회 성공", answers));
    }
}