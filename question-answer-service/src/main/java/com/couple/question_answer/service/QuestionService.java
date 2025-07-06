package com.couple.question_answer.service;

import com.couple.question_answer.dto.QuestionRequest;
import com.couple.question_answer.dto.QuestionResponse;
import com.couple.question_answer.entity.Question;
import com.couple.question_answer.entity.VectorChange;
import com.couple.question_answer.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {

        private final QuestionRepository questionRepository;

        public QuestionResponse createQuestion(QuestionRequest request) {
                log.info("질문 생성 요청: {}", request.getQuestion());

                // VectorChangeDto를 VectorChange로 변환
                List<VectorChange> vectorsA = request.getVectors_a().stream()
                                .map(dto -> VectorChange.builder()
                                                .dimension(dto.getDimension())
                                                .change(dto.getChange())
                                                .build())
                                .collect(Collectors.toList());

                List<VectorChange> vectorsB = request.getVectors_b().stream()
                                .map(dto -> VectorChange.builder()
                                                .dimension(dto.getDimension())
                                                .change(dto.getChange())
                                                .build())
                                .collect(Collectors.toList());

                Question question = Question.builder()
                                .question(request.getQuestion())
                                .date(request.getDate())
                                .choice_a(request.getChoice_a())
                                .vectors_a(vectorsA)
                                .choice_b(request.getChoice_b())
                                .vectors_b(vectorsB)
                                .tags(request.getTags())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                Question savedQuestion = questionRepository.save(question);
                log.info("질문 생성 완료: id={}", savedQuestion.getId());

                return convertToResponse(savedQuestion);
        }

        public List<QuestionResponse> getAllQuestions() {
                log.info("전체 질문 조회 요청");

                List<Question> questions = questionRepository.findAll();
                return questions.stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        public QuestionResponse getQuestionById(String questionId) {
                log.info("질문 조회 요청: questionId={}", questionId);

                Question question = questionRepository.findById(questionId)
                                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다: " + questionId));

                return convertToResponse(question);
        }

        public QuestionResponse getQuestionByDate(LocalDate date) {
                log.info("날짜별 질문 조회 요청: date={}", date);

                Question question = questionRepository.findByDate(date)
                                .orElseThrow(() -> new IllegalArgumentException("해당 날짜의 질문을 찾을 수 없습니다: " + date));

                return convertToResponse(question);
        }

        public List<QuestionResponse> getQuestionsByDate(LocalDate date) {
                log.info("날짜별 질문 목록 조회 요청: date={}", date);

                List<Question> questions = questionRepository.findAllByDateOrderByCreatedAtDesc(date);
                return questions.stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        private QuestionResponse convertToResponse(Question question) {
                return QuestionResponse.builder()
                                .id(question.getId())
                                .question(question.getQuestion())
                                .date(question.getDate())
                                .choiceA(question.getChoice_a())
                                .choiceB(question.getChoice_b())
                                .tags(question.getTags())
                                .createdAt(question.getCreatedAt())
                                .updatedAt(question.getUpdatedAt())
                                .build();
        }
}