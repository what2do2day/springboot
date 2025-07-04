package com.couple.schedule_meeting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

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
     * MongoDB에서 사용자의 취향 벡터를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 취향 벡터
     */
    public Map<String, Double> getUserPreferenceVector(String userId) {
        try {
            String url = userVectorServiceUrl + "/api/user-vectors/internal/" + userId;
            Map<String, Double> raw = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Double>>() {})
                .block();
            if (raw == null) return getDefaultPreferenceVector();
            // key 변환: vector1 → vec_1
            Map<String, Double> converted = new HashMap<>();
            for (Map.Entry<String, Double> entry : raw.entrySet()) {
                String newKey = entry.getKey().replace("vector", "vec_");
                converted.put(newKey, entry.getValue());
            }
            return converted;
        } catch (Exception e) {
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
} 