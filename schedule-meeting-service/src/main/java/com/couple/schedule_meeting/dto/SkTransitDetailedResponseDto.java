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
public class SkTransitDetailedResponseDto {
    
    private MetaData metaData;
    private Integer status;
    private String error;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetaData {
        private Plan plan;
        private RequestParameters requestParameters;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Plan {
        private List<DetailedItinerary> itineraries;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailedItinerary {
        private Integer totalTime;
        private Integer totalDistance;
        private Integer totalWalkTime;
        private Integer transferCount;
        private Integer totalWalkDistance;
        private Integer pathType;
        private List<DetailedLeg> legs;
        private Fare fare;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailedLeg {
        private String mode;
        private Integer sectionTime;
        private Integer distance;
        private Location start;
        private Location end;
        private List<Step> steps;
        private PassShape passShape;
        private String routeColor;
        private String route;
        private String routeId;
        private Integer service;
        private Integer type;
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
    public static class Step {
        private String streetName;
        private Integer distance;
        private String description;
        private String linestring;
        private Integer sectionTime;
        private String lon;
        private String lat;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassShape {
        private String lineString;
        private List<Coordinate> coordinates;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coordinate {
        private String lon;
        private String lat;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassStopList {
        private List<Stop> stationList;
        private List<Stop> busStops;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stop {
        private String stationName;
        private String lon;
        private String lat;
        private String stationID;
        private Integer index;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestParameters {
        private String startX;
        private String startY;
        private String endX;
        private String endY;
        private String locale;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Fare {
        private Regular regular;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Regular {
        private Integer totalFare;
    }
} 