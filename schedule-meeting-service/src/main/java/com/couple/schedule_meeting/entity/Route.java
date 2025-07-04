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
@Document(collection = "routes")
public class Route {
    
    @Id
    private String id;
    
    private Object routes; // TmpMeeting의 routes 값을 그대로 저장
    
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
        private List<RouteLeg> legs;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteLeg {
        private String mode;
        private Integer sectionTime;
        private Integer distance;
        private Location start;
        private Location end;
        private List<RouteStep> steps;
        private PassShape passShape;
        private String routeColor;
        private String route;
        private String routeId;
        private Integer service;
        private Integer type;
        private List<Lane> lane;
        private PassStopList passStopList;
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
    public static class RouteStep {
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
    public static class PassStopList {
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
} 