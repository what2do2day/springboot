package com.couple.question_answer.service;

import com.couple.common.dto.ApiResponse;
import com.couple.question_answer.dto.CoupleInfoResponse;
import com.couple.question_answer.dto.CoupleVectorsResponse;
import com.couple.question_answer.dto.UserVectorRequest;
import com.couple.question_answer.dto.UserVectorResponse;
import com.couple.question_answer.entity.UserVector;
import com.couple.question_answer.repository.UserVectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserVectorService {

    private final UserVectorRepository userVectorRepository;
    private final WebClient webClient;
    
    @Value("${user-couple-service.url:http://localhost:8081}")
    private String userCoupleServiceUrl;

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
                throw new IllegalArgumentException("잘못된 벡터 키입니다: " + key + " (vec_1 ~ vec_50만 허용)");
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
            throw new IllegalArgumentException("잘못된 벡터 키입니다: " + vectorKey + " (vec_1 ~ vec_50만 허용)");
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

    public double getCurrentVectorValue(UUID userId, String vectorKey) {
        log.info("현재 벡터 값 조회: userId={}, vectorKey={}", userId, vectorKey);

        UserVector userVector = userVectorRepository.findByUserId(userId)
                .orElseGet(() -> UserVector.createInitialVector(userId));

        Map<String, Double> vectors = userVector.getVectors();
        return vectors.getOrDefault(vectorKey, 0.0);
    }

    public void deleteUserVector(UUID userId) {
        log.info("사용자 벡터 삭제: userId={}", userId);

        UserVector userVector = userVectorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 벡터를 찾을 수 없습니다: " + userId));

        userVectorRepository.delete(userVector);
        log.info("사용자 벡터 삭제 완료: userId={}", userId);
    }

    public CoupleVectorsResponse getCoupleVectors(UUID userId) {
        log.info("커플 벡터 조회 시작: userId={}", userId);

        try {
            // 1. user-couple-service에서 커플 정보 조회
            String coupleInfoUrl = userCoupleServiceUrl + "/api/couples/info";
            ApiResponse<CoupleInfoResponse> coupleResponse = webClient.get()
                    .uri(coupleInfoUrl)
                    .header("X-User-ID", userId.toString())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<CoupleInfoResponse>>() {})
                    .block();

            if (coupleResponse == null || !coupleResponse.isSuccess() || coupleResponse.getData() == null) {
                log.error("커플 정보 조회 실패: userId={}", userId);
                throw new RuntimeException("커플 정보를 조회할 수 없습니다.");
            }

            CoupleInfoResponse coupleInfo = coupleResponse.getData();
            log.info("커플 정보 조회 성공: coupleId={}, user1={}, user2={}", 
                    coupleInfo.getCoupleId(), coupleInfo.getUser1().getUserId(), coupleInfo.getUser2().getUserId());

            // 2. 각 사용자의 벡터 정보 조회
            CoupleVectorsResponse.CoupleUserVectorResponse user1Vector = getUserVectorInfo(coupleInfo.getUser1());
            CoupleVectorsResponse.CoupleUserVectorResponse user2Vector = getUserVectorInfo(coupleInfo.getUser2());

            // 3. 응답 생성
            CoupleVectorsResponse response = CoupleVectorsResponse.builder()
                    .coupleId(coupleInfo.getCoupleId())
                    .user1(user1Vector)
                    .user2(user2Vector)
                    .build();

            log.info("커플 벡터 조회 완료: coupleId={}", coupleInfo.getCoupleId());
            return response;

        } catch (Exception e) {
            log.error("커플 벡터 조회 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("커플 벡터 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private CoupleVectorsResponse.CoupleUserVectorResponse getUserVectorInfo(CoupleInfoResponse.UserInfo userInfo) {
        log.info("사용자 벡터 정보 조회: userId={}, name={}", userInfo.getUserId(), userInfo.getName());

        try {
            // 사용자 벡터 조회
            UserVector userVector = userVectorRepository.findByUserId(userInfo.getUserId())
                    .orElseGet(() -> UserVector.createInitialVector(userInfo.getUserId()));

            return CoupleVectorsResponse.CoupleUserVectorResponse.builder()
                    .userId(userInfo.getUserId())
                    .name(userInfo.getName())
                    .gender(userInfo.getGender())
                    .preferences(userVector.getVectors())
                    .build();

        } catch (Exception e) {
            log.error("사용자 벡터 정보 조회 실패: userId={}, error={}", userInfo.getUserId(), e.getMessage());
            // 벡터가 없는 경우 기본 벡터로 생성
            UserVector defaultVector = UserVector.createInitialVector(userInfo.getUserId());
            return CoupleVectorsResponse.CoupleUserVectorResponse.builder()
                    .userId(userInfo.getUserId())
                    .name(userInfo.getName())
                    .gender(userInfo.getGender())
                    .preferences(defaultVector.getVectors())
                    .build();
        }
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