package com.couple.question_answer.service;

import com.couple.question_answer.dto.UserAnswerRequest;
import com.couple.question_answer.dto.UserAnswerResponse;
import com.couple.question_answer.entity.Question;
import com.couple.question_answer.entity.UserAnswer;
import com.couple.question_answer.entity.VectorChange;
import com.couple.question_answer.repository.QuestionRepository;
import com.couple.question_answer.repository.UserAnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserAnswerService {

    private final UserAnswerRepository userAnswerRepository;
    private final QuestionRepository questionRepository;
    private final UserVectorService userVectorService;

    public UserAnswerResponse submitAnswer(UUID userId, UUID coupleId, UserAnswerRequest request) {
        log.info("답변 제출 요청 - userId: {}, coupleId: {}, questionId: {}, selectedChoice: {}",
                userId, coupleId, request.getQuestionId(), request.getSelectedChoice());

        // 1. 답변 저장
        UserAnswer userAnswer = UserAnswer.builder()
                .userId(userId)
                .questionId(request.getQuestionId())
                .coupleId(coupleId)
                .selectedChoice(request.getSelectedChoice())
                .build();

        UserAnswer savedAnswer = userAnswerRepository.save(userAnswer);
        log.info("답변 저장 완료: answerId={}", savedAnswer.getId());

        // 2. 질문 조회하여 벡터 변경값 확인
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다: " + request.getQuestionId()));

        // 3. 선택한 답변에 따른 벡터 변경값 추출
        List<VectorChange> vectorChanges = request.getSelectedChoice().equals("A")
                ? question.getVectors_a()
                : question.getVectors_b();

        // 4. 사용자 벡터 업데이트
        updateUserVectors(userId, vectorChanges);

        return convertToResponse(savedAnswer);
    }

    private void updateUserVectors(UUID userId, List<VectorChange> vectorChanges) {
        log.info("사용자 벡터 업데이트 시작 - userId: {}, vectorChanges: {}", userId, vectorChanges.size());

        for (VectorChange vectorChange : vectorChanges) {
            try {
                // 현재 벡터 값 조회
                double currentValue = userVectorService.getCurrentVectorValue(userId, vectorChange.getDimension());

                // 새로운 벡터 값 계산 (기존 값 + 변경값, -1.0 ~ 1.0 범위로 제한)
                double newValue = Math.max(-1.0, Math.min(1.0, currentValue + vectorChange.getChange()));

                // 벡터 업데이트
                userVectorService.updateSpecificVector(userId, vectorChange.getDimension(), newValue);

                log.info("벡터 업데이트 완료 - userId: {}, dimension: {}, currentValue: {}, change: {}, newValue: {}",
                        userId, vectorChange.getDimension(), currentValue, vectorChange.getChange(), newValue);

            } catch (Exception e) {
                log.error("벡터 업데이트 실패 - userId: {}, dimension: {}, error: {}",
                        userId, vectorChange.getDimension(), e.getMessage());
            }
        }
    }

    public List<UserAnswerResponse> getUserAnswers(UUID userId) {
        log.info("사용자 답변 조회 - userId: {}", userId);

        List<UserAnswer> answers = userAnswerRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return answers.stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    private UserAnswerResponse convertToResponse(UserAnswer userAnswer) {
        return UserAnswerResponse.builder()
                .id(userAnswer.getId())
                .userId(userAnswer.getUserId())
                .questionId(userAnswer.getQuestionId())
                .coupleId(userAnswer.getCoupleId())
                .selectedChoice(userAnswer.getSelectedChoice())
                .createdAt(userAnswer.getCreatedAt())
                .build();
    }
}