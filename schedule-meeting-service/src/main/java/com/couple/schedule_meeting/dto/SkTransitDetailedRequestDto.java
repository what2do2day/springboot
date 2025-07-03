package com.couple.schedule_meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkTransitDetailedRequestDto {
    private String startX;
    private String startY;
    private String endX;
    private String endY;
    private Integer lang;
    private String format;
    private Integer count;
    private Boolean includeDetailedStops;
} 