package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.KakaoRegionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoApiService {
    
    @Value("${kakao.api.key}")
    private String kakaoApiKey;
    
    private final WebClient webClient;
    
    public Mono<KakaoRegionResponse> getRegionFromCoordinates(double longitude, double latitude) {
        log.info("Kakao API 요청 시작 - longitude: {}, latitude: {}", longitude, latitude);
        log.info("Kakao API Key: {}", kakaoApiKey.substring(0, Math.min(10, kakaoApiKey.length())) + "...");
        
        String requestUrl = String.format("https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?x=%f&y=%f", longitude, latitude);
        log.info("Kakao API 요청 URL: {}", requestUrl);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/geo/coord2regioncode.json")
                        .queryParam("x", longitude)
                        .queryParam("y", latitude)
                        .build())
                .header("Authorization", "KakaoAK " + kakaoApiKey)
                .retrieve()
                .bodyToMono(KakaoRegionResponse.class)
                .doOnSuccess(response -> {
                    log.info("Kakao API 응답 성공");
                    if (response != null && response.getDocuments() != null) {
                        log.info("응답 문서 수: {}", response.getDocuments().size());
                        if (!response.getDocuments().isEmpty()) {
                            KakaoRegionResponse.Document doc = response.getDocuments().get(0);
                            log.info("첫 번째 문서 - region_type: {}, code: {}, address_name: {}", 
                                    doc.getRegionType(), doc.getCode(), doc.getAddressName());
                            log.info("행정구역 정보 - 1depth: {}, 2depth: {}, 3depth: {}", 
                                    doc.getRegion1depthName(), doc.getRegion2depthName(), doc.getRegion3depthName());
                        }
                    } else {
                        log.warn("Kakao API 응답이 null이거나 documents가 null입니다");
                    }
                })
                .doOnError(error -> {
                    log.error("Kakao API 요청 실패: {}", error.getMessage(), error);
                });
    }
} 