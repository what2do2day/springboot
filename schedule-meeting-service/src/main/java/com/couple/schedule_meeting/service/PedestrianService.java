package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.SkPedestrianResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedestrianService {

    private final WebClient webClient;
    
    private static final String PEDESTRIAN_API_URL = "https://apis.openapi.sk.com/tmap/routes/pedestrian";
    
    @Value("${sk.api.app-key}")
    private String skAppKey;

    /**
     * SK 보행자 경로 API를 통해 경로를 조회합니다.
     * 
     * @param startX 출발지 경도
     * @param startY 출발지 위도
     * @param endX 도착지 경도
     * @param endY 도착지 위도
     * @param startName 출발지 이름 (선택)
     * @param endName 도착지 이름 (선택)
     * @return 보행자 경로 정보
     */
    public SkPedestrianResponseDto getPedestrianRoute(String startX, String startY, String endX, String endY, 
                                                     String startName, String endName) {
        try {
            // startName, endName만 UTF-8 URL 인코딩
            String startNameEncoded = java.net.URLEncoder.encode(startName != null ? startName : "출발", "UTF-8");
            String endNameEncoded = java.net.URLEncoder.encode(endName != null ? endName : "도착", "UTF-8");

            // 직접 JSON 문자열 생성 (curl 예시와 동일)
            String jsonBody = String.format(
                "{\"startX\":\"%s\",\"startY\":\"%s\",\"endX\":\"%s\",\"endY\":\"%s\",\"startName\":\"%s\",\"endName\":\"%s\",\"reqCoordType\":\"WGS84GEO\",\"resCoordType\":\"WGS84GEO\",\"searchOption\":\"0\",\"sort\":\"index\"}",
                startX, startY, endX, endY, startNameEncoded, endNameEncoded
            );

            log.info("SK 보행자 경로 API 요청 전송: {}", PEDESTRIAN_API_URL);
            log.info("요청 데이터: {} -> {}", 
                    String.format("(%s, %s)", startX, startY), 
                    String.format("(%s, %s)", endX, endY));
            log.info("요청 JSON: {}", jsonBody);

            // WebClient를 사용하여 SK 보행자 경로 API에 요청 전송
            String responseBody = webClient.post()
                    .uri(PEDESTRIAN_API_URL)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header("appKey", skAppKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseBody != null) {
                log.info("SK 보행자 경로 API 응답 성공");
                // log.info("SK 보행자 경로 API 응답 성공: {}", responseBody); // 필요시 주석 해제
                // JSON 문자열을 DTO로 변환
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                SkPedestrianResponseDto response = objectMapper.readValue(responseBody, SkPedestrianResponseDto.class);
                return response;
            } else {
                log.warn("SK 보행자 경로 API 응답이 null입니다");
                return null;
            }

        } catch (WebClientResponseException e) {
            log.error("SK 보행자 경로 API HTTP 오류: {} - {}", e.getStatusCode(), e.getMessage());
            log.error("응답 바디: {}", e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("SK 보행자 경로 API 서비스 오류: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 위도/경도 문자열을 받아서 보행자 경로를 조회합니다.
     * 
     * @param startLatitude 출발지 위도
     * @param startLongitude 출발지 경도
     * @param endLatitude 도착지 위도
     * @param endLongitude 도착지 경도
     * @param startName 출발지 이름 (선택)
     * @param endName 도착지 이름 (선택)
     * @return 보행자 경로 정보
     */
    public SkPedestrianResponseDto getPedestrianRouteByLatLng(String startLatitude, String startLongitude, 
                                                             String endLatitude, String endLongitude,
                                                             String startName, String endName) {
        return getPedestrianRoute(startLongitude, startLatitude, endLongitude, endLatitude, startName, endName);
    }

    /**
     * 보행자 경로의 총 거리를 조회합니다.
     * 
     * @param response 보행자 경로 API 응답
     * @return 총 거리 (미터)
     */
    public Integer getTotalDistance(SkPedestrianResponseDto response) {
        if (response != null && response.getFeatures() != null) {
            for (SkPedestrianResponseDto.Feature feature : response.getFeatures()) {
                if (feature.getProperties() != null && feature.getProperties().getTotalDistance() != null) {
                    return feature.getProperties().getTotalDistance();
                }
            }
        }
        return 0;
    }

    /**
     * 보행자 경로의 총 소요 시간을 조회합니다.
     * 
     * @param response 보행자 경로 API 응답
     * @return 총 소요 시간 (초)
     */
    public Integer getTotalTime(SkPedestrianResponseDto response) {
        if (response != null && response.getFeatures() != null) {
            for (SkPedestrianResponseDto.Feature feature : response.getFeatures()) {
                if (feature.getProperties() != null && feature.getProperties().getTotalTime() != null) {
                    return feature.getProperties().getTotalTime();
                }
            }
        }
        return 0;
    }

    /**
     * 두 지점 간의 거리를 계산합니다 (미터 단위).
     * 
     * @param lat1 첫 번째 지점의 위도
     * @param lon1 첫 번째 지점의 경도
     * @param lat2 두 번째 지점의 위도
     * @param lon2 두 번째 지점의 경도
     * @return 두 지점 간의 거리 (미터)
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 지구의 반지름 (미터)
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    /**
     * 거리가 짧은지 판단합니다 (기본값: 1km 이하).
     * 
     * @param distance 거리 (미터)
     * @param threshold 임계값 (미터, 기본값: 1000)
     * @return 짧은 거리 여부
     */
    public boolean isShortDistance(double distance, double threshold) {
        return distance <= threshold;
    }

    /**
     * 거리가 짧은지 판단합니다 (기본 임계값: 1km).
     * 
     * @param distance 거리 (미터)
     * @return 짧은 거리 여부
     */
    public boolean isShortDistance(double distance) {
        return isShortDistance(distance, 1000.0);
    }
} 