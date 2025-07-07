package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.CoupleInfo;
import com.couple.schedule_meeting.dto.CoupleProfile;
import com.couple.schedule_meeting.dto.UserInfo;
import com.couple.schedule_meeting.dto.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 사용자 정보를 통합하여 제공하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final UserProfileService userProfileService;
    private final CoupleVectorService coupleVectorService;

    /**
     * 사용자 ID로 해당 사용자가 속한 커플의 기본 정보와 취향 벡터를 모두 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 커플 정보와 취향 벡터
     */
    public CoupleInfo getCoupleInfoByUserId(String userId) {
        log.info("사용자 ID로 커플 정보와 취향 조회 시작: userId={}", userId);

        try {
            // 새로운 커플 벡터 API를 사용하여 커플 정보와 취향벡터를 한 번에 조회
            CoupleVectorService.CoupleVectorsResponse coupleVectors = coupleVectorService.getCoupleVectors(userId);
            
            CoupleInfo coupleInfo = CoupleInfo.builder()
                    .coupleId(coupleVectors.getCoupleId().toString())
                    .user1(convertToUserInfo(coupleVectors.getUser1()))
                    .user2(convertToUserInfo(coupleVectors.getUser2()))
                    .build();

            log.info("커플 정보와 취향 조회 완료: userId={}, coupleId={}", userId, coupleVectors.getCoupleId());
            return coupleInfo;
            
        } catch (Exception e) {
            log.error("커플 벡터 API 호출 실패, 기존 방식으로 fallback: userId={}, error={}", userId, e.getMessage());
            
            // API 호출 실패 시 기존 방식으로 fallback
            CoupleProfile coupleProfile = userProfileService.getCoupleProfileByUserId(userId);

            CoupleInfo coupleInfo = CoupleInfo.builder()
                    .coupleId(coupleProfile.getCoupleId())
                    .user1(getUserInfoWithFallback(coupleProfile.getUser1().getId()))
                    .user2(getUserInfoWithFallback(coupleProfile.getUser2().getId()))
                    .build();

            log.info("fallback 방식으로 커플 정보와 취향 조회 완료: userId={}, coupleId={}", userId, coupleProfile.getCoupleId());
            return coupleInfo;
        }
    }

    /**
     * CoupleVectorService의 응답을 UserInfo로 변환합니다.
     */
    private UserInfo convertToUserInfo(CoupleVectorService.CoupleVectorsResponse.CoupleUserVectorResponse userVector) {
        return UserInfo.builder()
                .id(userVector.getUserId().toString())
                .gender(userVector.getGender())
                .birth("") // birth 정보는 API에서 제공하지 않으므로 빈 문자열
                .preferenceVector(userVector.getPreferences())
                .build();
    }

    /**
     * 사용자의 기본 정보와 취향 벡터를 모두 조회합니다. (fallback용)
     *
     * @param userId 사용자 ID
     * @return 사용자 정보와 취향 벡터
     */
    private UserInfo getUserInfoWithFallback(String userId) {
        log.info("fallback 방식으로 사용자 정보와 취향 조회 시작: userId={}", userId);

        // 기본 사용자 정보 조회
        UserProfile userProfile = userProfileService.getUserProfile(userId);

        // 기본 취향 벡터 (모든 값이 0.0)
        Map<String, Double> preferenceVector = new java.util.HashMap<>();
        for (int i = 1; i <= 50; i++) {
            preferenceVector.put("vec_" + i, 0.0);
        }

        // 통합 정보 생성
        UserInfo userInfo = UserInfo.builder()
                .id(userProfile.getId())
                .gender(userProfile.getGender())
                .birth(userProfile.getBirth())
                .preferenceVector(preferenceVector)
                .build();

        log.info("fallback 방식으로 사용자 정보와 취향 조회 완료: userId={}, gender={}, 벡터개수={}", 
                userId, userInfo.getGender(), preferenceVector.size());
        return userInfo;
    }
} 