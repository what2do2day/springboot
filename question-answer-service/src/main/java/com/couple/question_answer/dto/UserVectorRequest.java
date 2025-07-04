package com.couple.question_answer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVectorRequest {

    @NotNull(message = "벡터 데이터는 필수입니다.")
    private Map<String, Double> vectors;
}