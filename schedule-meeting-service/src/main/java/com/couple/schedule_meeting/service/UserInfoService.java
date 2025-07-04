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
    private final UserPreferenceService userPreferenceService;

    /**
     * 사용자 ID로 해당 사용자가 속한 커플의 기본 정보와 취향 벡터를 모두 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 커플 정보와 취향 벡터
     */
    public CoupleInfo getCoupleInfoByUserId(String userId) {
        log.info("사용자 ID로 커플 정보와 취향 조회 시작: userId={}", userId);

        CoupleProfile coupleProfile = userProfileService.getCoupleProfileByUserId(userId);

        CoupleInfo coupleInfo = CoupleInfo.builder()
                .coupleId(coupleProfile.getCoupleId())
                .user1(getUserInfo(coupleProfile.getUser1().getId()))
                .user2(getUserInfo(coupleProfile.getUser2().getId()))
                .build();

        log.info("사용자 ID로 커플 정보와 취향 조회 완료: userId={}, coupleId={}", userId, coupleProfile.getCoupleId());
        return coupleInfo;
    }

    /**
     * 사용자의 기본 정보와 취향 벡터를 모두 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 정보와 취향 벡터
     */
    private UserInfo getUserInfo(String userId) {
        log.info("사용자 정보와 취향 조회 시작: userId={}", userId);

        // 기본 사용자 정보 조회
        UserProfile userProfile = userProfileService.getUserProfile(userId);

        // 취향 벡터 조회
        Map<String, Double> preferenceVector = userPreferenceService.getUserPreferenceVector(userId);

        // 통합 정보 생성
        UserInfo userInfo = UserInfo.builder()
                .id(userProfile.getId())
                .gender(userProfile.getGender())
                .birth(userProfile.getBirth())
                .preferenceVector(preferenceVector)
                .build();

        log.info("사용자 정보와 취향 조회 완료: userId={}, gender={}", userId, userInfo.getGender());
        return userInfo;
    }
} 