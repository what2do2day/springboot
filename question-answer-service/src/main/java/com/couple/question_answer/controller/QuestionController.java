package com.couple.question_answer.controller;

import com.couple.common.dto.ApiResponse;
import com.couple.question_answer.dto.QuestionRequest;
import com.couple.question_answer.dto.QuestionResponse;
import com.couple.question_answer.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(name = "Question", description = "질문 관련 API")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    @Operation(summary = "질문 생성", description = "새로운 질문을 생성합니다.")
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId,
            @Valid @RequestBody QuestionRequest request) {
        log.info("질문 생성 요청 - userId: {}, coupleId: {}, question: {}", userId, coupleId, request.getQuestion());

        QuestionResponse response = questionService.createQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("질문이 생성되었습니다.", response));
    }

    @GetMapping
    @Operation(summary = "전체 질문 조회", description = "모든 질문을 조회합니다.")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getAllQuestions(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId) {
        log.info("전체 질문 조회 요청 - userId: {}, coupleId: {}", userId, coupleId);

        List<QuestionResponse> questions = questionService.getAllQuestions();
        return ResponseEntity.ok(ApiResponse.success("질문 목록 조회 성공", questions));
    }

    @GetMapping("/{questionId}")
    @Operation(summary = "질문 상세 조회", description = "특정 질문의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<QuestionResponse>> getQuestionById(
            @Parameter(description = "질문 ID") @PathVariable UUID questionId,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId) {
        log.info("질문 상세 조회 - userId: {}, coupleId: {}, questionId: {}", userId, coupleId, questionId);

        QuestionResponse response = questionService.getQuestionById(questionId);
        return ResponseEntity.ok(ApiResponse.success("질문 조회 성공", response));
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "날짜별 질문 조회", description = "특정 날짜의 질문들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsByDate(
            @Parameter(description = "날짜 (yyyy-MM-dd)") @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId) {
        log.info("날짜별 질문 조회 - userId: {}, coupleId: {}, date: {}", userId, coupleId, date);

        List<QuestionResponse> questions = questionService.getQuestionsByDate(date);
        return ResponseEntity.ok(ApiResponse.success("날짜별 질문 조회 성공", questions));
    }

    @GetMapping("/unsent/{date}")
    @Operation(summary = "미전송 질문 조회", description = "특정 날짜의 미전송 질문들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getUnsentQuestionsByDate(
            @Parameter(description = "날짜 (yyyy-MM-dd)") @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId) {
        log.info("미전송 질문 조회 - userId: {}, coupleId: {}, date: {}", userId, coupleId, date);

        List<QuestionResponse> questions = questionService.getUnsentQuestionsByDate(date);
        return ResponseEntity.ok(ApiResponse.success("미전송 질문 조회 성공", questions));
    }

    @PutMapping("/{questionId}/mark-sent")
    @Operation(summary = "질문 전송 완료 처리", description = "질문을 전송 완료 상태로 변경합니다.")
    public ResponseEntity<ApiResponse<String>> markQuestionAsSent(
            @Parameter(description = "질문 ID") @PathVariable UUID questionId,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId) {
        log.info("질문 전송 완료 처리 - userId: {}, coupleId: {}, questionId: {}", userId, coupleId, questionId);

        questionService.markQuestionAsSent(questionId);
        return ResponseEntity.ok(ApiResponse.success("질문 전송 완료 처리 성공", null));
    }

    @GetMapping("/test-headers")
    @Operation(summary = "헤더 테스트", description = "전달된 헤더 정보를 확인합니다.")
    public ResponseEntity<ApiResponse<String>> testHeaders(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId) {
        log.info("헤더 테스트 - userId: {}, coupleId: {}", userId, coupleId);

        String response = String.format("헤더 전달 확인 - userId: %s, coupleId: %s", userId, coupleId);
        return ResponseEntity.ok(ApiResponse.success("헤더 테스트 성공", response));
    }
}