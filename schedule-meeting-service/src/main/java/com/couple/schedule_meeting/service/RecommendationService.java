package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.RecommendationRequest;
import com.couple.schedule_meeting.dto.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    
    private final WebClient webClient;
    
    // TODO: 실제 URL로 변경 필요
    private static final String RECOMMENDATION_API_URL = "http://localhost:8083/api/recommend";
    
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
} 