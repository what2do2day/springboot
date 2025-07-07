package com.couple.schedule_meeting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty("time_slots")
    private List<TimeSlot> timeSlots;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlot {
        private String slot;
        @JsonProperty("top_candidates")
        private List<StoreCandidate> topCandidates;
        @JsonProperty("llm_recommendation")
        private LlmRecommendation llmRecommendation;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreCandidate {
        @JsonProperty("store_name")
        private String storeName;
        private Double score;
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