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
public class WaypointRouteResponse {
    
    private List<RouteSegment> segments;
    private RouteSummary summary;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteSegment {
        private Integer sequence;
        private String fromName;
        private String toName;
        private String fromLon;
        private String fromLat;
        private String toLon;
        private String toLat;
        private Integer totalTime;
        private Integer totalDistance;
        private Integer totalFare;
        private Integer totalWalkTime;
        private Integer transferCount;
        private String routeType;
        private List<Object> legs; // WALK, SUBWAY, BUS 등 모드별로 다른 구조
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteSummary {
        private Integer totalTime;
        private Integer totalDistance;
        private Integer totalFare;
        private Integer totalWalkTime;
        private Integer totalTransferCount;
        private Integer segmentCount;
        private List<String> waypointNames;
    }
    
    // WALK 모드용 Leg
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalkLeg {
        private String mode;
        private Integer sectionTime;
        private Integer distance;
        private Location start;
        private Location end;
        private List<WalkStep> steps;
        private PassShape passShape;
    }
    
    // SUBWAY 모드용 Leg
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubwayLeg {
        private String mode;
        private String routeColor;
        private Integer sectionTime;
        private String route;
        private String routeId;
        private Integer distance;
        private Integer service;
        private Location start;
        private SubwayPassStopList passStopList;
        private Location end;
        private Integer type;
        private PassShape passShape;
    }
    
    // BUS 모드용 Leg
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusLeg {
        private String routeColor;
        private Integer distance;
        private Location start;
        private List<Lane> lane;
        private Integer type;
        private String mode;
        private Integer sectionTime;
        private String route;
        private String routeId;
        private Integer service;
        private BusPassStopList passStopList;
        private Location end;
        private PassShape passShape;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private String name;
        private String lon;
        private String lat;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalkStep {
        private String streetName;
        private Integer distance;
        private String description;
        private String linestring;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassShape {
        private String lineString;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Lane {
        private String routeColor;
        private String route;
        private String routeId;
        private Integer service;
        private Integer type;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubwayPassStopList {
        private List<Station> stationList;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusPassStopList {
        private List<Station> stationList;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Station {
        private Integer index;
        private String stationName;
        private String lon;
        private String lat;
        private String stationId;
    }
} 