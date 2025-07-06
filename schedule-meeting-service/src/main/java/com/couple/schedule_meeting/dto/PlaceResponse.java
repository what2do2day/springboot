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
public class PlaceResponse {
    private String id;
    private String name;
    private String address;
    private BigDecimal rating;
    private String category;

    public static PlaceResponse from(Place place) {
        return PlaceResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .address(place.getAddress())
                .rating(place.getRating())
                .category(place.getCategory())
                .build();
    }
} 