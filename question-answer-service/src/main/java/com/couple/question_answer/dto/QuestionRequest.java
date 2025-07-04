package com.couple.question_answer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {

    @NotBlank(message = "질문은 필수입니다.")
    private String question;

    @NotBlank(message = "옵션1은 필수입니다.")
    private String option1;

    @NotBlank(message = "옵션2는 필수입니다.")
    private String option2;

    @NotNull(message = "전송 예정일은 필수입니다.")
    private LocalDate date;
}