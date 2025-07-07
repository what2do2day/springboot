package com.couple.question_answer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswerRequest {

    @NotBlank(message = "질문 ID는 필수입니다.")
    private String questionId;

    @NotBlank(message = "선택한 답변은 필수입니다.")
    private String selectedChoice; // 'A' 또는 'B'
}