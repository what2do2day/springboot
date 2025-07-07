package com.couple.schedule_meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectionRequest {
    private String currentLat; // 현재 위치 위도
    private String currentLon; // 현재 위치 경도
    private String placeId;    // 선택한 장소 ID
} 