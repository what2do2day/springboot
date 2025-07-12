package com.couple.schedule_meeting.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkPedestrianResponseDto {
    
    private String type;
    private List<Feature> features;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature {
        private String type;
        private Geometry geometry;
        private Properties properties;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Geometry {
        private String type;
        private List<List<Double>> coordinates; // LineString의 경우
        private List<Double> coordinatesPoint; // Point의 경우
        
        // JSON 응답에서 coordinates가 단일 배열로 올 수도 있음
        @com.fasterxml.jackson.annotation.JsonProperty("coordinates")
        public void setCoordinates(Object coordinates) {
            if (coordinates instanceof List) {
                List<?> coordList = (List<?>) coordinates;
                if (!coordList.isEmpty() && coordList.get(0) instanceof List) {
                    // LineString: [[lon, lat], [lon, lat], ...]
                    this.coordinates = (List<List<Double>>) coordinates;
                } else {
                    // Point: [lon, lat]
                    this.coordinatesPoint = (List<Double>) coordinates;
                }
            }
        }
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        private Integer totalDistance;
        private Integer totalTime;
        private Integer index;
        private Integer pointIndex;
        private String name;
        private String description;
        private String direction;
        private String nearPoiName;
        private String nearPoiX;
        private String nearPoiY;
        private String intersectionName;
        private String facilityType;
        private String facilityName;
        private Integer turnType;
        private String pointType;
        
        // LineString용 속성들
        private Integer lineIndex;
        private Integer distance;
        private Integer time;
        private Integer roadType;
        private Integer categoryRoadType;
    }
} 