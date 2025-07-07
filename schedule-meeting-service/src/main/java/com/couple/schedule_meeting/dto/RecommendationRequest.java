package com.couple.schedule_meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    
    private UserInfo user1;
    private UserInfo user2;
    private LocalDate date;
    private String weather;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<String> keywords;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        @JsonProperty("preferences")
        private Map<String, Double> preferences;
        private String gender;
    }
} 