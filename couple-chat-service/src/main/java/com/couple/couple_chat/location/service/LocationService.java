package com.couple.couple_chat.location.service;

import com.couple.couple_chat.location.dto.LocationShareRequest;
import com.couple.couple_chat.location.dto.LocationShareResponse;
import com.couple.couple_chat.location.entity.LocationHistory;
import com.couple.couple_chat.location.entity.UserLocation;
import com.couple.couple_chat.location.repository.LocationHistoryRepository;
import com.couple.couple_chat.location.repository.UserLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.couple.couple_chat.controller.WebSocketController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LocationService {

    private final UserLocationRepository userLocationRepository;
    private final LocationHistoryRepository locationHistoryRepository;

    /**
     * 위치 공유
     */
    public LocationShareResponse shareLocation(UUID senderId, LocationShareRequest request) {
        log.info("위치 공유 요청: userId={}, roomId={}, lat={}, lng={}", 
                senderId, request.getRoomId(), request.getLatitude(), request.getLongitude());

        // 현재 위치 업데이트
        UserLocation userLocation = updateUserLocation(senderId, request);

        // 위치 히스토리 저장
        LocationHistory locationHistory = saveLocationHistory(senderId, request);

        // WebSocket으로 실시간 전송 (표준 WebSocket 방식)
        LocationShareResponse response = convertToResponse(locationHistory);
        // TODO: 표준 WebSocket을 통한 메시지 전송 구현
        // 현재는 REST API 응답만 반환

        log.info("위치 공유 완료: userId={}, locationId={}", senderId, locationHistory.getId());
        return response;
    }

    /**
     * 사용자 현재 위치 업데이트
     */
    private UserLocation updateUserLocation(UUID userId, LocationShareRequest request) {
        Optional<UserLocation> existingLocation = userLocationRepository.findByUserId(userId);
        
        UserLocation userLocation;
        if (existingLocation.isPresent()) {
            // 기존 위치 업데이트
            userLocation = existingLocation.get();
            userLocation.setLatitude(request.getLatitude());
            userLocation.setLongitude(request.getLongitude());
            userLocation.setUpdatedAt(LocalDateTime.now());
        } else {
            // 새 위치 생성
            userLocation = UserLocation.builder()
                    .userId(userId)
                    .coupleId(request.getRoomId()) // roomId를 coupleId로 사용
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .isActive(true)
                    .build();
        }

        return userLocationRepository.save(userLocation);
    }

    /**
     * 위치 히스토리 저장
     */
    private LocationHistory saveLocationHistory(UUID userId, LocationShareRequest request) {
        LocationHistory locationHistory = LocationHistory.builder()
                .userId(userId)
                .coupleId(request.getRoomId()) // roomId를 coupleId로 사용
                .roomId(request.getRoomId())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        return locationHistoryRepository.save(locationHistory);
    }

    /**
     * 커플의 최신 위치 조회
     */
    @Transactional(readOnly = true)
    public List<LocationShareResponse> getCoupleLatestLocations(UUID coupleId) {
        List<UserLocation> locations = userLocationRepository.findLatestByCoupleId(coupleId);
        
        return locations.stream()
                .map(this::convertUserLocationToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 위치 조회
     */
    @Transactional(readOnly = true)
    public LocationShareResponse getUserLocation(UUID userId) {
        UserLocation userLocation = userLocationRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자 위치를 찾을 수 없습니다"));
        
        return convertUserLocationToResponse(userLocation);
    }

    /**
     * 위치 히스토리 조회
     */
    @Transactional(readOnly = true)
    public List<LocationShareResponse> getLocationHistory(UUID coupleId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LocationHistory> history = locationHistoryRepository
                .findByCoupleIdOrderByCreatedAtDesc(coupleId, pageable);

        return history.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 최근 위치 히스토리 조회 (지정된 시간 이후)
     */
    @Transactional(readOnly = true)
    public List<LocationShareResponse> getRecentLocationHistory(UUID coupleId, LocalDateTime since) {
        List<LocationHistory> history = locationHistoryRepository.findRecentByCoupleId(coupleId, since);
        
        return history.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * LocationHistory를 Response로 변환
     */
    private LocationShareResponse convertToResponse(LocationHistory locationHistory) {
        return LocationShareResponse.builder()
                .id(locationHistory.getId())
                .roomId(locationHistory.getRoomId())
                .senderId(locationHistory.getUserId())
                .senderName("사용자") // 실제로는 사용자 정보를 조회해야 함
                .latitude(locationHistory.getLatitude())
                .longitude(locationHistory.getLongitude())
                .timestamp(locationHistory.getCreatedAt())
                .messageType("LOCATION")
                .createdAt(locationHistory.getCreatedAt())
                .build();
    }

    /**
     * UserLocation을 Response로 변환
     */
    private LocationShareResponse convertUserLocationToResponse(UserLocation userLocation) {
        return LocationShareResponse.builder()
                .id(userLocation.getId())
                .roomId(userLocation.getCoupleId())
                .senderId(userLocation.getUserId())
                .senderName("사용자") // 실제로는 사용자 정보를 조회해야 함
                .latitude(userLocation.getLatitude())
                .longitude(userLocation.getLongitude())
                .timestamp(userLocation.getUpdatedAt())
                .messageType("LOCATION")
                .createdAt(userLocation.getCreatedAt())
                .build();
    }
} 