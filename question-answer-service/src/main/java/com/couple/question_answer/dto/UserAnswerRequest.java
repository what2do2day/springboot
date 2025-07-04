package com.couple.question_answer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswerRequest {

    @NotNull(message = "질문 ID는 필수입니다.")
    private UUID questionId;

    @NotBlank(message = "선택은 필수입니다.")
    @Pattern(regexp = "^[12]$", message = "선택은 1 또는 2만 가능합니다.")
    private String selectedOption; // '1' or '2'
}