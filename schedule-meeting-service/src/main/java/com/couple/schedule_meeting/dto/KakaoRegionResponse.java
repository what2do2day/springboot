package com.couple.schedule_meeting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class KakaoRegionResponse {
    private List<Document> documents;
    private Meta meta;

    @Data
    public static class Document {
        @JsonProperty("region_type")
        private String regionType;
        private String code;
        @JsonProperty("address_name")
        private String addressName;
        @JsonProperty("region_1depth_name")
        private String region1depthName;
        @JsonProperty("region_2depth_name")
        private String region2depthName;
        @JsonProperty("region_3depth_name")
        private String region3depthName;
        private double x;
        private double y;
    }

    @Data
    public static class Meta {
        @JsonProperty("total_count")
        private int totalCount;
    }
} 