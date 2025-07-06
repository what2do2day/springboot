package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.DirectionRequest;
import com.couple.schedule_meeting.dto.WaypointRouteRequest;
import com.couple.schedule_meeting.dto.WaypointRouteResponse;
import com.couple.schedule_meeting.entity.Place;
import com.couple.schedule_meeting.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

/**
 * 현재 위치에서 선택한 장소까지의 대중교통 상세 경로를 제공하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DirectionService {

    private final PlaceRepository placeRepository;
    private final WaypointRouteService waypointRouteService;

    /**
     * 현재 위치에서 선택한 장소까지의 대중교통 상세 경로를 조회합니다.
     * 
     * @param request 현재 위치와 선택한 장소 ID
     * @return 대중교통 상세 경로 정보
     */
    public WaypointRouteResponse getDirection(DirectionRequest request) {
        try {
            log.info("대중교통 경로 조회 시작: 현재위치({}, {}), 장소ID={}", 
                    request.getCurrentLat(), request.getCurrentLon(), request.getPlaceId());

            // 1. 선택한 장소의 정보를 DB에서 조회
            Optional<Place> placeOpt = placeRepository.findById(request.getPlaceId());
            if (placeOpt.isEmpty()) {
                log.warn("장소를 찾을 수 없습니다: placeId={}", request.getPlaceId());
                return null;
            }

            Place place = placeOpt.get();
            log.info("장소 정보 조회 성공: {} ({}, {})", 
                    place.getName(), place.getLatitude(), place.getLongitude());

            // 2. 현재 위치와 선택한 장소의 좌표로 경로 요청 생성
            WaypointRouteRequest.LocationCoordinate currentLocation = WaypointRouteRequest.LocationCoordinate.builder()
                    .name("현재 위치")
                    .lon(request.getCurrentLon())
                    .lat(request.getCurrentLat())
                    .build();

            WaypointRouteRequest.LocationCoordinate destinationLocation = WaypointRouteRequest.LocationCoordinate.builder()
                    .name(place.getName())
                    .lon(place.getLongitude() != null ? place.getLongitude().toString() : null)
                    .lat(place.getLatitude() != null ? place.getLatitude().toString() : null)
                    .build();

            WaypointRouteRequest waypointRequest = WaypointRouteRequest.builder()
                    .waypoints(Arrays.asList(currentLocation, destinationLocation))
                    .routeType("fastest")
                    .build();

            // 3. 기존 WaypointRouteService를 사용하여 상세 경로 조회
            WaypointRouteResponse routeResponse = waypointRouteService.getWaypointRoute(waypointRequest);

            if (routeResponse != null) {
                log.info("대중교통 경로 조회 성공: 총 {}분, {}m, {}원", 
                        routeResponse.getSummary().getTotalTime() / 60,
                        routeResponse.getSummary().getTotalDistance(),
                        routeResponse.getSummary().getTotalFare());
            } else {
                log.warn("대중교통 경로 조회 실패");
            }

            return routeResponse;

        } catch (Exception e) {
            log.error("대중교통 경로 조회 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
} 