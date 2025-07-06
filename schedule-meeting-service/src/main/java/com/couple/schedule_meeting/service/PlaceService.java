package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.entity.Place;
import com.couple.schedule_meeting.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceService {
    private final PlaceRepository placeRepository;

    public Place getPlaceById(String placeId) {
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("Place not found with id: " + placeId));
    }
} 