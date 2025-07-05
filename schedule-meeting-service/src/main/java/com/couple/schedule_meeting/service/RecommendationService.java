package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.RecommendationRequest;
import com.couple.schedule_meeting.dto.RecommendationResponse;
import com.couple.schedule_meeting.entity.Place;
import com.couple.schedule_meeting.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    
    private final WebClient webClient;
    private final PlaceRepository placeRepository;
    
    // TODO: URL로 확인 필요
    private static final String RECOMMENDATION_API_URL = "http://49.50.131.82:8000/api/v1/planner/generate-plan-vector";
    
    public RecommendationResponse getRecommendations(RecommendationRequest request) {
        log.info("외부 추천 API 호출 시작: {}", request);
        
        return webClient.post()
                .uri(RECOMMENDATION_API_URL)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RecommendationResponse.class)
                .doOnSuccess(response -> log.info("외부 추천 API 호출 성공"))
                .doOnError(error -> log.error("외부 추천 API 호출 실패: {}", error.getMessage()))
                .block(); // 동기 호출로 변경 (필요시 Mono<RecommendationResponse>로 변경 가능)
    }
    
    public Mono<RecommendationResponse> getRecommendationsAsync(RecommendationRequest request) {
        log.info("외부 추천 API 비동기 호출 시작: {}", request);
        
        return webClient.post()
                .uri(RECOMMENDATION_API_URL)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RecommendationResponse.class)
                .doOnSuccess(response -> log.info("외부 추천 API 호출 성공"))
                .doOnError(error -> log.error("외부 추천 API 호출 실패: {}", error.getMessage()));
    }
    
    /**
     * RecommendationResponse에서 각 TimeSlot의 첫 번째 StoreCandidate의 storeName으로
     * Place 테이블에서 위도/경도 정보를 조회하여 리스트로 반환
     * @param response 추천 응답 데이터
     * @return 위도/경도 쌍의 리스트 (TimeSlot 순서대로)
     */
    public List<LocationCoordinate> getLocationCoordinates(RecommendationResponse response) {
        List<LocationCoordinate> coordinates = new ArrayList<>();
        
        if (response == null || response.getTimeSlots() == null) {
            log.warn("추천 응답 데이터가 null이거나 timeSlots가 null입니다.");
            return coordinates;
        }
        
        for (RecommendationResponse.TimeSlot timeSlot : response.getTimeSlots()) {
            if (timeSlot.getTopCandidates() != null && !timeSlot.getTopCandidates().isEmpty()) {
                String storeName = timeSlot.getTopCandidates().get(0).getStoreName();
                Optional<Place> place = placeRepository.findByName(storeName);
                
                if (place.isPresent()) {
                    Place foundPlace = place.get();
                    coordinates.add(LocationCoordinate.builder()
                            .storeName(storeName)
                            .latitude(foundPlace.getLatitude())
                            .longitude(foundPlace.getLongitude())
                            .build());
                    log.debug("장소 '{}'의 좌표 조회 성공: ({}, {})", 
                            storeName, foundPlace.getLatitude(), foundPlace.getLongitude());
                } else {
                    log.warn("장소 '{}'를 데이터베이스에서 찾을 수 없습니다.", storeName);
                    // null 좌표로 추가하여 순서 유지
                    coordinates.add(LocationCoordinate.builder()
                            .storeName(storeName)
                            .latitude(null)
                            .longitude(null)
                            .build());
                }
            } else {
                log.warn("TimeSlot에 topCandidates가 없습니다.");
            }
        }
        
        log.info("총 {}개의 장소 좌표를 조회했습니다.", coordinates.size());
        return coordinates;
    }
    
    /**
     * 위도/경도 정보를 담는 내부 클래스
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LocationCoordinate {
        private String storeName;
        private BigDecimal latitude;
        private BigDecimal longitude;
    }
} 