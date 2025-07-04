package com.couple.schedule_meeting.dto;

import com.couple.schedule_meeting.entity.Meeting;
import com.couple.schedule_meeting.entity.MeetingPlace;
import com.couple.schedule_meeting.entity.Route;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingResponse {
    private UUID id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String date;
    private List<MeetingPlaceResponse> meetingPlaces;
    private Object route; // MongoDB에 저장된 포맷 그대로 사용

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingPlaceResponse {
        private UUID id;
        private String name;
        private String address;
        private String category;
        private String latitude;
        private String longitude;
        private Integer sequence;
    }

    public static MeetingResponse from(Meeting meeting, List<MeetingPlace> meetingPlaces, Route route) {
        List<MeetingPlaceResponse> placeResponses = meetingPlaces.stream()
                .map(place -> MeetingPlaceResponse.builder()
                        .id(place.getId())
                        .name(place.getName())
                        .address(place.getAddress())
                        .category(place.getCategory())
                        .latitude(place.getLatitude() != null ? place.getLatitude().toString() : null)
                        .longitude(place.getLongitude() != null ? place.getLongitude().toString() : null)
                        .sequence(place.getSequence())
                        .build())
                .toList();

        return MeetingResponse.builder()
                .id(meeting.getId())
                .name(meeting.getName())
                .startTime(meeting.getStartTime())
                .endTime(meeting.getEndTime())
                .date(meeting.getDate())
                .meetingPlaces(placeResponses)
                .route(route != null ? route.getRoutes() : null) // MongoDB에 저장된 routes 객체 그대로 사용
                .build();
    }
} 