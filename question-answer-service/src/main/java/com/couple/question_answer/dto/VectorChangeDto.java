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
public class VectorChangeDto {

    @NotBlank(message = "벡터 차원은 필수입니다.")
    private String dimension;

    @NotNull(message = "변경값은 필수입니다.")
    private Double change;
}