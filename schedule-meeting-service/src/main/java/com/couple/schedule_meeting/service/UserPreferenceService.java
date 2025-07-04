package com.couple.schedule_meeting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

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

    /**
     * MongoDB에서 사용자의 취향 벡터를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 취향 벡터
     */
    public Map<String, Double> getUserPreferenceVector(String userId) {
        try {
            log.info("MongoDB에서 취향 벡터 조회 시작: userId={}", userId);
            
            // TODO: 실제 컬렉션명과 필드명으로 수정 필요
            Query query = new Query(Criteria.where("userId").is(userId));
            Map<String, Object> result = mongoTemplate.findOne(query, Map.class, "user_preferences");
            
            if (result != null && result.containsKey("vector")) {
                @SuppressWarnings("unchecked")
                Map<String, Double> vector = (Map<String, Double>) result.get("vector");
                log.info("MongoDB에서 취향 벡터 조회 성공: userId={}", userId);
                return vector;
            } else {
                log.warn("MongoDB에서 취향 벡터를 찾을 수 없습니다: userId={}", userId);
                return getDefaultPreferenceVector();
            }
            
        } catch (Exception e) {
            log.error("MongoDB에서 취향 벡터 조회 중 오류: {}", e.getMessage(), e);
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