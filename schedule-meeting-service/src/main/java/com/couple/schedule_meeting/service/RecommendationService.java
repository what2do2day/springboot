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
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    
    private final WebClient webClient;
    private final PlaceRepository placeRepository;

    private static final String RECOMMENDATION_API_URL = "http://49.50.131.82:8000/api/v1/planner/generate-plan-vector";
    
    public RecommendationResponse getRecommendations(RecommendationRequest request) {
        log.info("=== 외부 추천 API 호출 시작 ===");
        log.info("요청 URL: {}", RECOMMENDATION_API_URL);
        
        // 요청 데이터 로깅
        log.info("요청 데이터:");
        log.info("  - user1: gender={}, preferences.size={}", 
                request.getUser1().getGender(), 
                request.getUser1().getPreferences().size());
        log.info("  - user2: gender={}, preferences.size={}", 
                request.getUser2().getGender(), 
                request.getUser2().getPreferences().size());
        log.info("  - date: {}", request.getDate());
        log.info("  - weather: {}", request.getWeather());
        log.info("  - startTime: {}", request.getStartTime());
        log.info("  - endTime: {}", request.getEndTime());
        log.info("  - keywords: {}", request.getKeywords());
        
        // 취향벡터 샘플 로깅 (첫 5개)
        log.info("  - user1 preferences 샘플: {}", 
                request.getUser1().getPreferences().entrySet().stream()
                        .limit(5)
                        .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        log.info("  - user2 preferences 샘플: {}", 
                request.getUser2().getPreferences().entrySet().stream()
                        .limit(5)
                        .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        
        return webClient.post()
                .uri(RECOMMENDATION_API_URL)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RecommendationResponse.class)
                .doOnSuccess(response -> {
                    log.info("=== 외부 추천 API 호출 성공 ===");
                    if (response != null) {
                        log.info("응답 데이터:");
                        log.info("  - timeSlots.size: {}", 
                                response.getTimeSlots() != null ? response.getTimeSlots().size() : "null");
                        if (response.getTimeSlots() != null && !response.getTimeSlots().isEmpty()) {
                            log.info("  - 첫 번째 timeSlot: slot={}, topCandidates.size={}", 
                                    response.getTimeSlots().get(0).getSlot(),
                                    response.getTimeSlots().get(0).getTopCandidates() != null ? 
                                            response.getTimeSlots().get(0).getTopCandidates().size() : "null");
                        }
                    } else {
                        log.warn("응답이 null입니다.");
                    }
                })
                .doOnError(error -> {
                    log.error("=== 외부 추천 API 호출 실패 ===");
                    log.error("에러 메시지: {}", error.getMessage());
                    if (error instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                        org.springframework.web.reactive.function.client.WebClientResponseException wcre = 
                            (org.springframework.web.reactive.function.client.WebClientResponseException) error;
                        log.error("HTTP 상태 코드: {}", wcre.getStatusCode());
                        log.error("응답 본문: {}", wcre.getResponseBodyAsString());
                        log.error("응답 헤더: {}", wcre.getHeaders());
                    }
                })
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
     * RecommendationResponse에서 각 TimeSlot의 llm_recommendation.selected 값으로
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
            if (timeSlot.getLlmRecommendation() != null && timeSlot.getLlmRecommendation().getSelected() != null) {
                String selectedStoreName = timeSlot.getLlmRecommendation().getSelected();
                Optional<Place> place = placeRepository.findByName(selectedStoreName);
                
                if (place.isPresent()) {
                    Place foundPlace = place.get();
                    coordinates.add(LocationCoordinate.builder()
                            .storeName(selectedStoreName)
                            .latitude(foundPlace.getLatitude())
                            .longitude(foundPlace.getLongitude())
                            .build());
                    log.info("LLM 추천 장소 '{}'의 좌표 조회 성공: ({}, {})", 
                            selectedStoreName, foundPlace.getLatitude(), foundPlace.getLongitude());
                } else {
                    log.warn("LLM 추천 장소 '{}'를 데이터베이스에서 찾을 수 없습니다.", selectedStoreName);
                    // null 좌표로 추가하여 순서 유지
                    coordinates.add(LocationCoordinate.builder()
                            .storeName(selectedStoreName)
                            .latitude(null)
                            .longitude(null)
                            .build());
                }
            } else {
                log.warn("TimeSlot에 llm_recommendation이 없거나 selected 값이 null입니다.");
            }
        }
        
        log.info("총 {}개의 LLM 추천 장소 좌표를 조회했습니다.", coordinates.size());
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