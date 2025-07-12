package com.couple.question_answer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoupleResponse {

    private UUID coupleId;
    private CoupleUserVectorResponse user1;
    private CoupleUserVectorResponse user2;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoupleUserVectorResponse {
        private UUID userId;
        private String name;
        private String gender;
        private Map<String, Double> preferences;
    }
} 