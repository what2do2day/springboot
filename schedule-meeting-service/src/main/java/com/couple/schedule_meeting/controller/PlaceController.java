package com.couple.schedule_meeting.controller;

import com.couple.common.dto.ApiResponse;
import com.couple.schedule_meeting.dto.PlaceRankResponse;
import com.couple.schedule_meeting.dto.PlaceResponse;
import com.couple.schedule_meeting.entity.Place;
import com.couple.schedule_meeting.service.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
@Slf4j
public class PlaceController {
    private final PlaceService placeService;

    @GetMapping("/{placeId}")
    public ResponseEntity<ApiResponse<PlaceResponse>> getPlace(@PathVariable String placeId) {
        Place place = placeService.getPlaceById(placeId);
        return ResponseEntity.ok(ApiResponse.success(PlaceResponse.from(place)));
    }
    
    @GetMapping("/ranks")
    public ResponseEntity<ApiResponse<List<PlaceRankResponse>>> getPlaceRanks(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam String code) {
        log.info("Received request - lat: {}, lon: {}, code: {}", lat, lon, code);
        
        // 유효성 검사
        if (lat == 0.0 && lon == 0.0) {
            log.warn("Invalid coordinates: lat={}, lon={}", lat, lon);
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid coordinates"));
        }
        if (!code.equals("P") && !code.equals("F")) {
            log.warn("Invalid code: {}", code);
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid code. Must be 'P' or 'F'"));
        }
        
        List<PlaceRankResponse> places = placeService.getPlaceRanks(lat, lon, code);
        return ResponseEntity.ok(ApiResponse.success("장소 랭킹 조회 성공", places));
    }
} 