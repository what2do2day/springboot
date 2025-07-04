package com.couple.question_answer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVectorResponse {

    private String id;
    private UUID userId;
    private Map<String, Double> vectors;
    private LocalDateTime updatedAt;
}