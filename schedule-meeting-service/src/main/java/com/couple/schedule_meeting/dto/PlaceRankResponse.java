package com.couple.schedule_meeting.dto;

import com.couple.schedule_meeting.entity.Place;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceRankResponse {
    private String id;
    private String name;
    private String address;
    private BigDecimal rating;
    private String code;
    private String category;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public static PlaceRankResponse from(Place place) {
        return PlaceRankResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .address(place.getAddress())
                .rating(place.getRating())
                .code(place.getCode())
                .category(place.getCategory())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .build();
    }
} 