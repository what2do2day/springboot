package com.couple.question_answer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {

    @NotBlank(message = "질문은 필수입니다.")
    private String question;

    @NotNull(message = "날짜는 필수입니다.")
    private LocalDate date;

    @NotBlank(message = "선택지 A는 필수입니다.")
    private String choice_a;

    @NotNull(message = "벡터 A는 필수입니다.")
    private List<VectorChangeDto> vectors_a;

    @NotBlank(message = "선택지 B는 필수입니다.")
    private String choice_b;

    @NotNull(message = "벡터 B는 필수입니다.")
    private List<VectorChangeDto> vectors_b;

    private List<String> tags;
}