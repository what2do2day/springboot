package com.couple.question_answer.service;

import com.couple.question_answer.dto.UserAnswerRequest;
import com.couple.question_answer.dto.UserAnswerResponse;
import com.couple.question_answer.entity.QuestionTag;
import com.couple.question_answer.entity.UserAnswer;
import com.couple.question_answer.entity.UserTagProfile;
import com.couple.question_answer.entity.UserTagProfileId;
import com.couple.question_answer.repository.QuestionTagRepository;
import com.couple.question_answer.repository.UserAnswerRepository;
import com.couple.question_answer.repository.UserTagProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserAnswerService {

    private final UserAnswerRepository userAnswerRepository;
    private final QuestionTagRepository questionTagRepository;
    private final UserTagProfileRepository userTagProfileRepository;

    public UserAnswerResponse submitAnswer(UUID userId, UserAnswerRequest request) {
        log.info("사용자 답변 제출: userId={}, questionId={}, choice={}",
                userId, request.getQuestionId(), request.getChoice());

        // 답변 저장
        UserAnswer userAnswer = UserAnswer.builder()
                .userId(userId)
                .questionId(request.getQuestionId())
                .choice(request.getChoice())
                .build();

        UserAnswer savedAnswer = userAnswerRepository.save(userAnswer);

        // 태그 점수 업데이트
        updateUserTagScores(userId, request.getQuestionId(), request.getChoice());

        return convertToResponse(savedAnswer);
    }

    public List<UserAnswerResponse> getUserAnswers(UUID userId) {
        log.info("사용자 답변 조회: {}", userId);

        List<UserAnswer> answers = userAnswerRepository.findByUserId(userId);
        return answers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private void updateUserTagScores(UUID userId, UUID questionId, String choice) {
        log.info("사용자 태그 점수 업데이트: userId={}, questionId={}, choice={}",
                userId, questionId, choice);

        // 질문에 연결된 태그들 조회
        List<QuestionTag> questionTags = questionTagRepository.findByQuestionId(questionId);

        for (QuestionTag questionTag : questionTags) {
            UUID tagId = questionTag.getId().getTagId();
            Float tagValue = questionTag.getValue();

            // 선택에 따라 점수 조정 (옵션1 선택 시 양수, 옵션2 선택 시 음수)
            float scoreChange = "1".equals(choice) ? tagValue : -tagValue;

            // 사용자 태그 프로필 조회 또는 생성
            UserTagProfile userTagProfile = userTagProfileRepository
                    .findByUserIdAndTagId(userId, tagId);

            if (userTagProfile == null) {
                // 새로운 프로필 생성
                UserTagProfileId profileId = new UserTagProfileId();
                profileId.setUserId(userId);
                profileId.setTagId(tagId);
                userTagProfile = UserTagProfile.builder()
                        .id(profileId)
                        .score(scoreChange)
                        .build();
            } else {
                // 기존 점수에 추가
                float newScore = userTagProfile.getScore() + scoreChange;
                // -1 ~ +1 범위로 제한
                newScore = Math.max(-1.0f, Math.min(1.0f, newScore));
                userTagProfile.setScore(newScore);
            }

            userTagProfileRepository.save(userTagProfile);
        }
    }

    private UserAnswerResponse convertToResponse(UserAnswer userAnswer) {
        return UserAnswerResponse.builder()
                .id(userAnswer.getId())
                .userId(userAnswer.getUserId())
                .questionId(userAnswer.getQuestionId())
                .choice(userAnswer.getChoice())
                .answeredAt(userAnswer.getAnsweredAt())
                .build();
    }
}