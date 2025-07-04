package com.couple.question_answer.service;

import com.couple.question_answer.dto.QuestionRequest;
import com.couple.question_answer.dto.QuestionResponse;
import com.couple.question_answer.entity.Question;
import com.couple.question_answer.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionResponse createQuestion(QuestionRequest request) {
        log.info("질문 생성 요청: {}", request.getQuestion());

        Question question = Question.builder()
                .question(request.getQuestion())
                .option1(request.getOption1())
                .option2(request.getOption2())
                .date(request.getDate())
                .sentYn("N")
                .build();

        Question savedQuestion = questionRepository.save(question);
        return convertToResponse(savedQuestion);
    }

    public List<QuestionResponse> getQuestionsByDate(LocalDate date) {
        log.info("날짜별 질문 조회: {}", date);

        List<Question> questions = questionRepository.findQuestionsByDate(date);
        return questions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<QuestionResponse> getUnsentQuestionsByDate(LocalDate date) {
        log.info("미전송 질문 조회: {}", date);

        List<Question> questions = questionRepository.findUnsentQuestionsByDate(date);
        return questions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public void markQuestionAsSent(UUID questionId) {
        log.info("질문 전송 완료 처리: {}", questionId);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("질문을 찾을 수 없습니다: " + questionId));

        question.setSentYn("Y");
        question.setSentTime(LocalDateTime.now());
        questionRepository.save(question);
    }

    public QuestionResponse getQuestionById(UUID questionId) {
        log.info("질문 조회: {}", questionId);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("질문을 찾을 수 없습니다: " + questionId));

        return convertToResponse(question);
    }

    public List<QuestionResponse> getAllQuestions() {
        log.info("전체 질문 조회");

        List<Question> questions = questionRepository.findAll();
        return questions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private QuestionResponse convertToResponse(Question question) {
        return QuestionResponse.builder()
                .id(question.getId())
                .question(question.getQuestion())
                .option1(question.getOption1())
                .option2(question.getOption2())
                .sentYn(question.getSentYn())
                .sentTime(question.getSentTime())
                .date(question.getDate())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .build();
    }
}