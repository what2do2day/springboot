package com.couple.question_answer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswerResponse {

    private UUID id;
    private UUID userId;
    private String questionId;
    private UUID coupleId;
    private String selectedChoice;
    private LocalDateTime createdAt;
}