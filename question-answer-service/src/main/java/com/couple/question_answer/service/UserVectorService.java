package com.couple.question_answer.service;

import com.couple.question_answer.dto.UserVectorRequest;
import com.couple.question_answer.dto.UserVectorResponse;
import com.couple.question_answer.entity.UserVector;
import com.couple.question_answer.repository.UserVectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserVectorService {

    private final UserVectorRepository userVectorRepository;

    public UserVectorResponse createUserVector(UUID userId) {
        log.info("사용자 벡터 생성: userId={}", userId);

        // 이미 존재하는지 확인
        if (userVectorRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("이미 존재하는 사용자 벡터입니다: " + userId);
        }

        // 초기 벡터 생성 (모든 값이 0.0)
        UserVector userVector = UserVector.createInitialVector(userId);
        UserVector savedVector = userVectorRepository.save(userVector);

        log.info("사용자 벡터 생성 완료: userId={}, id={}", userId, savedVector.getId());
        return convertToResponse(savedVector);
    }

    public UserVectorResponse getUserVector(UUID userId) {
        log.info("사용자 벡터 조회: userId={}", userId);

        UserVector userVector = userVectorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 벡터를 찾을 수 없습니다: " + userId));

        return convertToResponse(userVector);
    }

    public UserVectorResponse updateUserVector(UUID userId, UserVectorRequest request) {
        log.info("사용자 벡터 업데이트: userId={}", userId);

        UserVector userVector = userVectorRepository.findByUserId(userId)
                .orElseGet(() -> UserVector.createInitialVector(userId));

        // 벡터 값 검증 및 업데이트
        Map<String, Double> newVectors = request.getVectors();
        for (Map.Entry<String, Double> entry : newVectors.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();

            // 벡터 키 검증
            if (!userVector.isValidVectorKey(key)) {
                throw new IllegalArgumentException("잘못된 벡터 키입니다: " + key + " (vec1 ~ vec50만 허용)");
            }

            // 벡터 값 검증
            if (!userVector.isValidVectorValue(value)) {
                throw new IllegalArgumentException("잘못된 벡터 값입니다: " + value + " (-1.0 ~ 1.0 범위만 허용)");
            }

            userVector.updateVector(key, value);
        }

        UserVector savedVector = userVectorRepository.save(userVector);
        log.info("사용자 벡터 업데이트 완료: userId={}, id={}", userId, savedVector.getId());

        return convertToResponse(savedVector);
    }

    public UserVectorResponse updateSpecificVector(UUID userId, String vectorKey, Double value) {
        log.info("특정 벡터 업데이트: userId={}, vectorKey={}, value={}", userId, vectorKey, value);

        UserVector userVector = userVectorRepository.findByUserId(userId)
                .orElseGet(() -> UserVector.createInitialVector(userId));

        // 벡터 키 검증
        if (!userVector.isValidVectorKey(vectorKey)) {
            throw new IllegalArgumentException("잘못된 벡터 키입니다: " + vectorKey + " (vec1 ~ vec50만 허용)");
        }

        // 벡터 값 검증
        if (!userVector.isValidVectorValue(value)) {
            throw new IllegalArgumentException("잘못된 벡터 값입니다: " + value + " (-1.0 ~ 1.0 범위만 허용)");
        }

        userVector.updateVector(vectorKey, value);
        UserVector savedVector = userVectorRepository.save(userVector);

        log.info("특정 벡터 업데이트 완료: userId={}, vectorKey={}, value={}", userId, vectorKey, value);
        return convertToResponse(savedVector);
    }

    public void deleteUserVector(UUID userId) {
        log.info("사용자 벡터 삭제: userId={}", userId);

        UserVector userVector = userVectorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 벡터를 찾을 수 없습니다: " + userId));

        userVectorRepository.delete(userVector);
        log.info("사용자 벡터 삭제 완료: userId={}", userId);
    }

    private UserVectorResponse convertToResponse(UserVector userVector) {
        return UserVectorResponse.builder()
                .id(userVector.getId())
                .userId(userVector.getUserId())
                .vectors(userVector.getVectors())
                .updatedAt(userVector.getUpdatedAt())
                .build();
    }
}