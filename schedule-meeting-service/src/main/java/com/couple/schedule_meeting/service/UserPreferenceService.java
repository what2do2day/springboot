package com.couple.schedule_meeting.service;

import com.couple.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * MongoDB에서 사용자 취향 정보를 조회하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final MongoTemplate mongoTemplate;
    @Value("${user-vector-service.url:http://localhost:8086}")
    private String userVectorServiceUrl;
    private final WebClient webClient;

    /**
     * question-answer-service에서 사용자의 취향 벡터를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 취향 벡터
     */
    public Map<String, Double> getUserPreferenceVector(String userId) {
        try {
            String url = userVectorServiceUrl + "/api/user-vectors/my-vector";
            ApiResponse<UserPreferenceService.UserVectorResponse> response = webClient.get()
                .uri(url)
                .header("X-User-ID", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserPreferenceService.UserVectorResponse>>() {})
                .block();
            
            if (response == null || response.getData() == null || response.getData().getVectors() == null) {
                log.warn("취향 벡터 응답이 null입니다: userId={}", userId);
                return getDefaultPreferenceVector();
            }
            
            Map<String, Double> vectors = response.getData().getVectors();
            // key 변환: vector1 → vec_1
            Map<String, Double> converted = new HashMap<>();
            for (Map.Entry<String, Double> entry : vectors.entrySet()) {
                String newKey = entry.getKey().replace("vector", "vec_");
                converted.put(newKey, entry.getValue());
            }
            
            log.info("취향 벡터 조회 성공: userId={}, 벡터 개수={}", userId, converted.size());
            return converted;
        } catch (Exception e) {
            log.error("취향 벡터 조회 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            return getDefaultPreferenceVector();
        }
    }

    /**
     * 기본 취향 벡터를 반환합니다.
     * MongoDB에 접근할 수 없을 때 사용됩니다.
     * 
     * @return 기본 취향 벡터
     */
    private Map<String, Double> getDefaultPreferenceVector() {
        log.info("기본 취향 벡터 사용");
        Map<String, Double> defaultVector = new HashMap<>();
        defaultVector.put("key1", 0.5);
        defaultVector.put("key2", 0.3);
        return defaultVector;
    }
    
    /**
     * 테스트용 하드코딩된 취향 벡터를 반환합니다.
     * 외부 API 호출 없이 테스트 데이터를 제공합니다.
     * 
     * @param userId 사용자 ID (userId의 해시값을 기반으로 다른 벡터 생성)
     * @return 하드코딩된 취향 벡터
     */
    public Map<String, Double> getHardcodedPreferenceVector(String userId) {
        log.info("하드코딩된 취향 벡터 사용: userId={}", userId);
        
        Map<String, Double> hardcodedVector = new HashMap<>();
        
        // userId의 해시값을 기반으로 다른 패턴의 벡터 생성
        int hash = userId.hashCode();
        int pattern = Math.abs(hash) % 3; // 0, 1, 2 중 하나의 패턴 선택
        
        // 50개의 취향 벡터 데이터 (vec_1 ~ vec_50)
        for (int i = 1; i <= 50; i++) {
            double value;
            
            switch (pattern) {
                case 0: // 패턴 1: 높은 선호도 (0.6 ~ 1.0)
                    value = 0.6 + (i * 0.008) + (i % 4 * 0.05);
                    break;
                case 1: // 패턴 2: 중간 선호도 (0.3 ~ 0.7)
                    value = 0.3 + (i * 0.008) + (i % 5 * 0.08);
                    break;
                case 2: // 패턴 3: 낮은 선호도 (0.1 ~ 0.5)
                    value = 0.1 + (i * 0.008) + (i % 3 * 0.1);
                    break;
                default:
                    value = 0.5;
            }
            
            if (value > 1.0) value = 1.0;
            if (value < 0.0) value = 0.0;
            
            hardcodedVector.put("vec_" + i, value);
        }
        
        log.info("하드코딩된 취향 벡터 생성 완료: userId={}, 패턴={}, {}개 항목", 
                userId, pattern, hardcodedVector.size());
        return hardcodedVector;
    }
    
    /**
     * UserVectorResponse 내부 클래스
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserVectorResponse {
        private String id;
        private UUID userId;
        private Map<String, Double> vectors;
        private LocalDateTime updatedAt;
    }
} 