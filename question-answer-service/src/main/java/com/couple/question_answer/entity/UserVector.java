package com.couple.question_answer.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Document(collection = "user_vectors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVector {

    @Id
    private String id;

    @Field("userId")
    private UUID userId;

    @Field("vectors")
    private Map<String, Double> vectors;

    @Field("updatedAt")
    private LocalDateTime updatedAt;

    // 벡터 초기화 메서드 (vec_1 ~ vec_50 형식)
    public static UserVector createInitialVector(UUID userId) {
        Map<String, Double> initialVectors = new java.util.HashMap<>();
        for (int i = 1; i <= 50; i++) {
            initialVectors.put("vec_" + i, 0.02);
        }

        return UserVector.builder()
                .userId(userId)
                .vectors(initialVectors)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // 특정 벡터 값 업데이트
    public void updateVector(String vectorKey, Double value) {
        if (vectors == null) {
            vectors = new java.util.HashMap<>();
        }
        vectors.put(vectorKey, value);
        updatedAt = LocalDateTime.now();
    }

    // 벡터 값 검증 (-1 ~ 1 범위)
    public boolean isValidVectorValue(Double value) {
        return value != null && value >= -1.0 && value <= 1.0;
    }

    // 벡터 키 검증 (vec_1 ~ vec_50)
    public boolean isValidVectorKey(String key) {
        if (key == null || !key.startsWith("vec_")) {
            return false;
        }
        try {
            int num = Integer.parseInt(key.substring(4)); // "vec_" 제거 후 숫자 추출
            return num >= 1 && num <= 50;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}