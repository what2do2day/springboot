package com.couple.schedule_meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingPlaceRequest {

    private String name;
    private String placeId;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer visitOrder;
    private Integer estimatedTime;
    private String category;
}