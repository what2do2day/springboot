package com.couple.couple_chat.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageClassificationResponse {
    private UUID messageId;
    private ClassificationResult classification; // 구조화된 감정분석 결과
} 