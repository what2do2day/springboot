package com.couple.schedule_meeting.service;

import com.couple.common.dto.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;

/**
 * question-answer-service의 커플 벡터 API를 호출하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoupleVectorService {

    private final WebClient webClient;
    
    @Value("${question-answer-service.url:http://localhost:8086}")
    private String questionAnswerServiceUrl;

    /**
     * 커플 벡터 정보를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 커플 벡터 정보
     */
    public CoupleVectorsResponse getCoupleVectors(String userId) {
        try {
            String url = questionAnswerServiceUrl + "/api/user-vectors/couple-vectors";
            ApiResponse<CoupleVectorsResponse> response = webClient.get()
                    .uri(url)
                    .header("X-User-ID", userId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<CoupleVectorsResponse>>() {})
                    .block();
            
            if (response == null || !response.isSuccess() || response.getData() == null) {
                log.error("커플 벡터 응답이 null입니다: userId={}", userId);
                throw new RuntimeException("커플 벡터를 조회할 수 없습니다.");
            }
            
            log.info("커플 벡터 조회 성공: userId={}, coupleId={}", userId, response.getData().getCoupleId());
            return response.getData();
            
        } catch (Exception e) {
            log.error("커플 벡터 조회 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("커플 벡터 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * CoupleVectorsResponse 내부 클래스
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CoupleVectorsResponse {
        private UUID coupleId;
        private CoupleUserVectorResponse user1;
        private CoupleUserVectorResponse user2;

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class CoupleUserVectorResponse {
            private UUID userId;
            private String name;
            private String gender;
            private Map<String, Double> preferences;
        }
    }
} 