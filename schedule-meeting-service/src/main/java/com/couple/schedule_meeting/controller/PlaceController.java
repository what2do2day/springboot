package com.couple.schedule_meeting.controller;

import com.couple.schedule_meeting.dto.PlaceResponse;
import com.couple.schedule_meeting.entity.Place;
import com.couple.schedule_meeting.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;

    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceResponse> getPlace(@PathVariable String placeId) {
        Place place = placeService.getPlaceById(placeId);
        return ResponseEntity.ok(PlaceResponse.from(place));
    }
} 