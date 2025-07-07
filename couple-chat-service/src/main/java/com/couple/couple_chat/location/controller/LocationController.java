package com.couple.couple_chat.location.controller;

import com.couple.couple_chat.location.dto.LocationShareRequest;
import com.couple.couple_chat.location.dto.LocationShareResponse;
import com.couple.couple_chat.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    /**
     * WebSocket을 통한 위치 공유
     */
    @MessageMapping("/share-location")
    public void shareLocation(@Payload LocationShareRequest request) {
        // 실제로는 인증된 사용자 ID를 가져와야 함
        UUID senderId = UUID.randomUUID(); // 임시
        locationService.shareLocation(senderId, request);
    }

    /**
     * REST API를 통한 위치 공유
     */
    @PostMapping("/share")
    public ResponseEntity<LocationShareResponse> shareLocation(
            @RequestHeader("X-User-ID") UUID senderId,
            @Valid @RequestBody LocationShareRequest request) {
        
        LocationShareResponse response = locationService.shareLocation(senderId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 커플의 최신 위치 조회
     */
    @GetMapping("/couple/{coupleId}/latest")
    public ResponseEntity<List<LocationShareResponse>> getCoupleLatestLocations(
            @PathVariable UUID coupleId) {
        
        List<LocationShareResponse> locations = locationService.getCoupleLatestLocations(coupleId);
        return ResponseEntity.ok(locations);
    }

    /**
     * 특정 사용자의 위치 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<LocationShareResponse> getUserLocation(@PathVariable UUID userId) {
        LocationShareResponse location = locationService.getUserLocation(userId);
        return ResponseEntity.ok(location);
    }

    /**
     * 위치 히스토리 조회
     */
    @GetMapping("/couple/{coupleId}/history")
    public ResponseEntity<List<LocationShareResponse>> getLocationHistory(
            @PathVariable UUID coupleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        List<LocationShareResponse> history = locationService.getLocationHistory(coupleId, page, size);
        return ResponseEntity.ok(history);
    }

    /**
     * 최근 위치 히스토리 조회 (지정된 시간 이후)
     */
    @GetMapping("/couple/{coupleId}/recent")
    public ResponseEntity<List<LocationShareResponse>> getRecentLocationHistory(
            @PathVariable UUID coupleId,
            @RequestParam LocalDateTime since) {
        
        List<LocationShareResponse> history = locationService.getRecentLocationHistory(coupleId, since);
        return ResponseEntity.ok(history);
    }
} 