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
public class UserTagProfileResponse {

    private UUID userId;
    private UUID tagId;
    private String tagName;
    private Float score;
    private LocalDateTime updatedAt;
}