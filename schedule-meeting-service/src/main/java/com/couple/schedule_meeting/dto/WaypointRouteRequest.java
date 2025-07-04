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
public class WaypointRouteRequest {
    
    private List<LocationCoordinate> waypoints;
    private String routeType; // fastest, shortest, etc.
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationCoordinate {
        private String name;
        private String lon;
        private String lat;
    }
} 