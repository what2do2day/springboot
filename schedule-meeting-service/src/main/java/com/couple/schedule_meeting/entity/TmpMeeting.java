package com.couple.schedule_meeting.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tmp_meetings")
public class TmpMeeting {
    
    @Id
    private String id;
    
    private String name;
    private String startTime;
    private String endTime;
    private String date;
    private List<String> keyword;
    private String weather;
    private String currentLat;
    private String currentLon;
    private MeetingResults results;
    private List<String> stores;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeetingResults {
        private List<TimeSlot> timeSlots;
        private Object routes; // routes는 나중에 별도 구조로 정의
    }
    
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