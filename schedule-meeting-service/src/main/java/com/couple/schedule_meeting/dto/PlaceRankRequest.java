package com.couple.schedule_meeting.dto;

import lombok.Data;

@Data
public class PlaceRankRequest {
    private double lat;  // 위도
    private double lon;  // 경도
    private String code; // P/F
} 