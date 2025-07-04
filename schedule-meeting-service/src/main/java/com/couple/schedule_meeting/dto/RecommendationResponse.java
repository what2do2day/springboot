package com.couple.schedule_meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    
    private List<TimeSlot> timeSlots;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlot {
        private String slot;
        private List<StoreCandidate> topCandidates;
        private LlmRecommendation llmRecommendation;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreCandidate {
        private String storeName;
        private Double score;
        private Double similarity;
        private String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LlmRecommendation {
        private String selected;
        private String reason;
    }
} 