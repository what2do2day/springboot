package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.SkTransitDetailedResponseDto;
import com.couple.schedule_meeting.dto.WaypointRouteRequest;
import com.couple.schedule_meeting.dto.WaypointRouteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 경유지 경로 서비스
 * 위도/경도 리스트를 받아서 상세 경로를 생성합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WaypointRouteService {

    private final TransitDetailedService transitDetailedService;

    /**
     * 경유지를 포함한 상세 경로를 조회합니다.
     * 각 구간별로 상세 경로(모든 정거장 포함)를 계산합니다.
     * 
     * @param request 경유지 요청 정보
     * @return 경유지 상세 경로 정보
     */
    public WaypointRouteResponse getWaypointRoute(WaypointRouteRequest request) {
        try {
            List<WaypointRouteRequest.LocationCoordinate> waypoints = request.getWaypoints();
            
            if (waypoints == null || waypoints.size() < 2) {
                log.warn("경유지가 2개 미만입니다. 최소 2개의 경유지가 필요합니다.");
                return null;
            }

            log.info("경유지 상세 경로 조회 시작: {}개 경유지", waypoints.size());
            
            List<WaypointRouteResponse.RouteSegment> segments = new ArrayList<>();
            int totalTime = 0;
            int totalDistance = 0;
            int totalFare = 0;
            int totalWalkTime = 0;
            int totalTransferCount = 0;
            List<String> waypointNames = new ArrayList<>();

            // 각 구간별로 상세 경로 조회
            for (int i = 0; i < waypoints.size() - 1; i++) {
                WaypointRouteRequest.LocationCoordinate from = waypoints.get(i);
                WaypointRouteRequest.LocationCoordinate to = waypoints.get(i + 1);
                
                // null 좌표 체크
                if (from.getLon() == null || from.getLat() == null || to.getLon() == null || to.getLat() == null) {
                    log.warn("구간 {} 좌표가 null입니다: {} -> {}", i + 1, from.getName(), to.getName());
                    continue; // 이 구간은 건너뛰고 다음 구간으로
                }
                
                log.info("구간 {} 상세 경로 조회: {} -> {}", i + 1, from.getName(), to.getName());
                
                // 상세 경로 조회
                SkTransitDetailedResponseDto detailedRouteResponse = transitDetailedService.getDetailedTransitRoute(
                    from.getLon(), from.getLat(), to.getLon(), to.getLat()
                );
                
                if (detailedRouteResponse != null && detailedRouteResponse.getMetaData() != null && 
                    detailedRouteResponse.getMetaData().getPlan() != null && 
                    detailedRouteResponse.getMetaData().getPlan().getItineraries() != null &&
                    !detailedRouteResponse.getMetaData().getPlan().getItineraries().isEmpty()) {
                    
                    SkTransitDetailedResponseDto.DetailedItinerary itinerary = 
                        detailedRouteResponse.getMetaData().getPlan().getItineraries().get(0);
                    
                    // 구간 정보 생성 (상세 경로 포함)
                    WaypointRouteResponse.RouteSegment segment = WaypointRouteResponse.RouteSegment.builder()
                            .sequence(i + 1)
                            .fromName(from.getName())
                            .toName(to.getName())
                            .fromLon(from.getLon())
                            .fromLat(from.getLat())
                            .toLon(to.getLon())
                            .toLat(to.getLat())
                            .totalTime(itinerary.getTotalTime())
                            .totalDistance(itinerary.getTotalDistance())
                            .totalFare(itinerary.getFare() != null && itinerary.getFare().getRegular() != null ? 
                                      itinerary.getFare().getRegular().getTotalFare() : 0)
                            .totalWalkTime(itinerary.getTotalWalkTime())
                            .transferCount(itinerary.getTransferCount())
                            .routeType(request.getRouteType() != null ? request.getRouteType() : "fastest")
                            .build();
                    
                    // legs 필드를 별도로 설정 (모드별 DTO 사용)
                    segment.setLegs(convertDetailedLegs(itinerary.getLegs()));
                    
                    segments.add(segment);
                    
                    // 전체 통계 누적
                    totalTime += itinerary.getTotalTime() != null ? itinerary.getTotalTime() : 0;
                    totalDistance += itinerary.getTotalDistance() != null ? itinerary.getTotalDistance() : 0;
                    totalFare += itinerary.getFare() != null && itinerary.getFare().getRegular() != null ? 
                                itinerary.getFare().getRegular().getTotalFare() : 0;
                    totalWalkTime += itinerary.getTotalWalkTime() != null ? itinerary.getTotalWalkTime() : 0;
                    totalTransferCount += itinerary.getTransferCount() != null ? itinerary.getTransferCount() : 0;
                    
                    if (i == 0) {
                        waypointNames.add(from.getName());
                    }
                    waypointNames.add(to.getName());
                    
                    log.info("구간 {} 상세 경로 완료: {}분, {}m, {}원", i + 1, 
                            itinerary.getTotalTime() != null ? itinerary.getTotalTime() / 60 : 0,
                            itinerary.getTotalDistance() != null ? itinerary.getTotalDistance() : 0,
                            itinerary.getFare() != null && itinerary.getFare().getRegular() != null ? 
                            itinerary.getFare().getRegular().getTotalFare() : 0);
                    
                } else {
                    log.warn("구간 {} 상세 경로 조회 실패: {} -> {}", i + 1, from.getName(), to.getName());
                    return null;
                }
            }
            
            // 전체 요약 정보 생성
            WaypointRouteResponse.RouteSummary summary = WaypointRouteResponse.RouteSummary.builder()
                    .totalTime(totalTime)
                    .totalDistance(totalDistance)
                    .totalFare(totalFare)
                    .totalWalkTime(totalWalkTime)
                    .totalTransferCount(totalTransferCount)
                    .segmentCount(segments.size())
                    .waypointNames(waypointNames)
                    .build();
            
            log.info("경유지 상세 경로 조회 완료: 총 {}분, {}m, {}원, {}회 환승", 
                    totalTime / 60, totalDistance, totalFare, totalTransferCount);
            
            return WaypointRouteResponse.builder()
                    .segments(segments)
                    .summary(summary)
                    .build();
                    
        } catch (Exception e) {
            log.error("경유지 상세 경로 조회 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 상세 경로 API 응답의 DetailedLeg를 경유지 응답 DTO의 Leg로 변환합니다.
     * 모드별로 다른 DTO 구조를 사용합니다.
     */
    private List<Object> convertDetailedLegs(List<SkTransitDetailedResponseDto.DetailedLeg> originalLegs) {
        if (originalLegs == null) {
            return new ArrayList<>();
        }
        
        return originalLegs.stream()
                .map(leg -> {
                    if ("WALK".equals(leg.getMode())) {
                        return convertToWalkLeg(leg);
                    } else if ("SUBWAY".equals(leg.getMode())) {
                        return convertToSubwayLeg(leg);
                    } else if ("BUS".equals(leg.getMode())) {
                        return convertToBusLeg(leg);
                    } else {
                        // 기존 방식으로 변환 (호환성)
                        return convertToLeg(leg);
                    }
                })
                .collect(Collectors.toList());
    }
    
    /**
     * WALK 모드용 Leg로 변환
     */
    private WaypointRouteResponse.WalkLeg convertToWalkLeg(SkTransitDetailedResponseDto.DetailedLeg leg) {
        List<WaypointRouteResponse.WalkStep> steps = convertToWalkSteps(leg.getSteps());
        
        return WaypointRouteResponse.WalkLeg.builder()
                .mode("WALK")
                .sectionTime(leg.getSectionTime())
                .distance(leg.getDistance())
                .start(convertDetailedLocation(leg.getStart()))
                .end(convertDetailedLocation(leg.getEnd()))
                .steps(steps)
                .passShape(convertPassShape(leg.getPassShape()))
                .build();
    }
    
    /**
     * SUBWAY 모드용 Leg로 변환
     */
    private WaypointRouteResponse.SubwayLeg convertToSubwayLeg(SkTransitDetailedResponseDto.DetailedLeg leg) {
        List<WaypointRouteResponse.Station> stationList = new ArrayList<>();
        
        if (leg.getPassStopList() != null && leg.getPassStopList().getStationList() != null) {
            stationList = leg.getPassStopList().getStationList().stream()
                    .map(stop -> WaypointRouteResponse.Station.builder()
                            .index(stop.getIndex())
                            .stationName(stop.getStationName())
                            .lon(stop.getLon())
                            .lat(stop.getLat())
                            .stationId(stop.getStationID())
                            .build())
                    .collect(Collectors.toList());
        }
        
        return WaypointRouteResponse.SubwayLeg.builder()
                .mode("SUBWAY")
                .routeColor(leg.getRouteColor())
                .sectionTime(leg.getSectionTime())
                .route(leg.getRoute())
                .routeId(leg.getRouteId())
                .distance(leg.getDistance())
                .service(leg.getService())
                .start(convertDetailedLocation(leg.getStart()))
                .passStopList(WaypointRouteResponse.SubwayPassStopList.builder()
                        .stationList(stationList)
                        .build())
                .end(convertDetailedLocation(leg.getEnd()))
                .type(leg.getType())
                .passShape(convertPassShape(leg.getPassShape()))
                .build();
    }
    
    /**
     * BUS 모드용 Leg로 변환
     */
    private WaypointRouteResponse.BusLeg convertToBusLeg(SkTransitDetailedResponseDto.DetailedLeg leg) {
        List<WaypointRouteResponse.Station> stationList = new ArrayList<>();
        
        if (leg.getPassStopList() != null && leg.getPassStopList().getStationList() != null) {
            stationList = leg.getPassStopList().getStationList().stream()
                    .map(stop -> WaypointRouteResponse.Station.builder()
                            .index(stop.getIndex())
                            .stationName(stop.getStationName())
                            .lon(stop.getLon())
                            .lat(stop.getLat())
                            .stationId(stop.getStationID())
                            .build())
                    .collect(Collectors.toList());
        }
        
        List<WaypointRouteResponse.Lane> lanes = new ArrayList<>();
        if (leg.getRouteColor() != null || leg.getRoute() != null || leg.getRouteId() != null) {
            lanes.add(WaypointRouteResponse.Lane.builder()
                    .routeColor(leg.getRouteColor())
                    .route(leg.getRoute())
                    .routeId(leg.getRouteId())
                    .service(leg.getService())
                    .type(leg.getType())
                    .build());
        }
        
        return WaypointRouteResponse.BusLeg.builder()
                .routeColor(leg.getRouteColor())
                .distance(leg.getDistance())
                .start(convertDetailedLocation(leg.getStart()))
                .lane(lanes)
                .type(leg.getType())
                .mode("BUS")
                .sectionTime(leg.getSectionTime())
                .route(leg.getRoute())
                .routeId(leg.getRouteId())
                .service(leg.getService())
                .passStopList(WaypointRouteResponse.BusPassStopList.builder()
                        .stationList(stationList)
                        .build())
                .end(convertDetailedLocation(leg.getEnd()))
                .passShape(convertPassShape(leg.getPassShape()))
                .build();
    }
    
    /**
     * 기존 방식의 Leg로 변환 (호환성)
     * 현재는 사용하지 않으므로 null 반환
     */
    private Object convertToLeg(SkTransitDetailedResponseDto.DetailedLeg leg) {
        // 기존 방식의 Leg 구조가 필요하면 여기에 구현
        return null;
    }
    
    /**
     * WALK 모드용 Step으로 변환
     */
    private List<WaypointRouteResponse.WalkStep> convertToWalkSteps(List<SkTransitDetailedResponseDto.Step> originalSteps) {
        if (originalSteps == null) {
            return new ArrayList<>();
        }
        
        return originalSteps.stream()
                .map(step -> WaypointRouteResponse.WalkStep.builder()
                        .streetName(step.getStreetName())
                        .distance(step.getDistance())
                        .description(step.getDescription())
                        .linestring(step.getLinestring())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 상세 경로 API 응답의 Location을 경유지 응답 DTO의 Location으로 변환합니다.
     */
    private WaypointRouteResponse.Location convertDetailedLocation(SkTransitDetailedResponseDto.Location originalLocation) {
        if (originalLocation == null) {
            return null;
        }
        
        return WaypointRouteResponse.Location.builder()
                .name(originalLocation.getName())
                .lon(originalLocation.getLon())
                .lat(originalLocation.getLat())
                .build();
    }

    /**
     * 상세 경로 API 응답의 PassShape를 경유지 응답 DTO의 PassShape로 변환합니다.
     */
    private WaypointRouteResponse.PassShape convertPassShape(SkTransitDetailedResponseDto.PassShape originalPassShape) {
        if (originalPassShape == null) {
            return null;
        }
        
        return WaypointRouteResponse.PassShape.builder()
                .lineString(originalPassShape.getLineString())
                .build();
    }
} 