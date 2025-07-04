package com.couple.question_answer.controller;

import com.couple.common.dto.ApiResponse;
import com.couple.question_answer.dto.UserAnswerRequest;
import com.couple.question_answer.dto.UserAnswerResponse;
import com.couple.question_answer.service.UserAnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "UserAnswer", description = "사용자 답변 관련 API")
public class UserAnswerController {

    private final UserAnswerService userAnswerService;

    @PostMapping
    @Operation(summary = "답변 제출", description = "사용자가 질문에 답변을 제출합니다.")
    public ResponseEntity<ApiResponse<UserAnswerResponse>> submitAnswer(
            @Parameter(description = "사용자 ID") @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "커플 ID") @RequestHeader(value = "X-Couple-ID", required = false) String coupleId,
            @Valid @RequestBody UserAnswerRequest request) {
        log.info("답변 제출 요청 - userId: {}, coupleId: {}, questionId: {}", userId, coupleId, request.getQuestionId());

        UserAnswerResponse response = userAnswerService.submitAnswer(UUID.fromString(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("답변이 제출되었습니다.", response));
    }

    @GetMapping("/my-answers")
    @Operation(summary = "내 답변 조회", description = "현재 사용자의 모든 답변을 조회합니다.")
    public ResponseEntity<ApiResponse<List<UserAnswerResponse>>> getMyAnswers(
            @Parameter(description = "사용자 ID") @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "커플 ID") @RequestHeader(value = "X-Couple-ID", required = false) String coupleId) {
        log.info("내 답변 조회 - userId: {}, coupleId: {}", userId, coupleId);

        List<UserAnswerResponse> answers = userAnswerService.getUserAnswers(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("답변 목록 조회 성공", answers));
    }
}