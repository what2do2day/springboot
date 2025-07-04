package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.SkTransitDetailedRequestDto;
import com.couple.schedule_meeting.dto.SkTransitDetailedResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * SK 교통 API 상세 경로 서비스
 * 모든 정거장 정보를 포함하는 버전
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkTransitDetailedService {

    private final WebClient webClient;
    
    @Value("${sk.api.url}")
    private String skApiUrl;

    @Value("${sk.api.app-key}")
    private String skAppKey;

    /**
     * SK 교통 API를 통해 상세 경로를 조회합니다.
     * 모든 정거장 정보를 포함합니다.
     * 
     * @param startX 출발지 경도
     * @param startY 출발지 위도
     * @param endX 도착지 경도
     * @param endY 도착지 위도
     * @return 상세 경로 정보 (모든 정거장 포함)
     */
    public SkTransitDetailedResponseDto getDetailedTransitRoute(String startX, String startY, String endX, String endY) {
        try {
            // 요청 DTO 생성
            SkTransitDetailedRequestDto requestDto = SkTransitDetailedRequestDto.builder()
                    .startX(startX)
                    .startY(startY)
                    .endX(endX)
                    .endY(endY)
                    .lang(0)
                    .format("json")
                    .count(10)
                    .includeDetailedStops(true)
                    .build();

            log.info("SK 교통 API 상세 경로 요청 전송: {}", skApiUrl);
            log.info("요청 데이터: {} -> {}", 
                    String.format("(%s, %s)", startX, startY), 
                    String.format("(%s, %s)", endX, endY));

            // WebClient를 사용하여 SK 교통 API에 요청 전송
            SkTransitDetailedResponseDto response = webClient.post()
                    .uri(skApiUrl)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header("appKey", skAppKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .retrieve()
                    .bodyToMono(SkTransitDetailedResponseDto.class)
                    .block();

            if (response != null && response.getMetaData() != null && 
                response.getMetaData().getPlan() != null && 
                response.getMetaData().getPlan().getItineraries() != null &&
                !response.getMetaData().getPlan().getItineraries().isEmpty()) {
                
                log.info("SK 교통 API 상세 경로 응답 성공");
                log.info("전체 경로 개수: {}", response.getMetaData().getPlan().getItineraries().size());
                
                // 최단 시간 경로만 추출
                SkTransitDetailedResponseDto fastestRoute = getFastestDetailedRoute(response);
                if (fastestRoute != null) {
                    log.info("최단 시간 상세 경로 추출 완료: {}분", 
                            fastestRoute.getMetaData().getPlan().getItineraries().get(0).getTotalTime() / 60);
                    return fastestRoute;
                }
                return response;
            } else {
                log.warn("SK 교통 API 상세 경로 응답이 없습니다.");
                return null;
            }

        } catch (WebClientResponseException e) {
            log.error("SK 교통 API HTTP 오류: {} - {}", e.getStatusCode(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("SK 교통 API 상세 경로 서비스 오류: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 응답에서 최단 시간 상세 경로만 추출하여 새로운 응답을 생성합니다.
     * 
     * @param response 원본 SK 교통 API 상세 응답
     * @return 최단 시간 상세 경로만 포함된 응답
     */
    private SkTransitDetailedResponseDto getFastestDetailedRoute(SkTransitDetailedResponseDto response) {
        if (response == null || response.getMetaData() == null || 
            response.getMetaData().getPlan() == null || 
            response.getMetaData().getPlan().getItineraries() == null ||
            response.getMetaData().getPlan().getItineraries().isEmpty()) {
            return null;
        }

        List<SkTransitDetailedResponseDto.DetailedItinerary> itineraries = response.getMetaData().getPlan().getItineraries();
        
        // 최단 시간 경로 찾기
        SkTransitDetailedResponseDto.DetailedItinerary fastestItinerary = itineraries.stream()
                .filter(itinerary -> itinerary.getTotalTime() != null)
                .min((a, b) -> Integer.compare(a.getTotalTime(), b.getTotalTime()))
                .orElse(null);

        if (fastestItinerary == null) {
            return null;
        }

        // 최단 시간 상세 경로만 포함된 새로운 응답 생성
        SkTransitDetailedResponseDto.Plan plan = SkTransitDetailedResponseDto.Plan.builder()
                .itineraries(List.of(fastestItinerary))
                .build();

        SkTransitDetailedResponseDto.MetaData metaData = SkTransitDetailedResponseDto.MetaData.builder()
                .plan(plan)
                .requestParameters(response.getMetaData().getRequestParameters())
                .build();

        return SkTransitDetailedResponseDto.builder()
                .metaData(metaData)
                .build();
    }
} 