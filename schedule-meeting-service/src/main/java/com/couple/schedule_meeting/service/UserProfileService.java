package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.CoupleProfile;
import com.couple.schedule_meeting.dto.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * user-couple-service에서 사용자 프로필 정보를 가져오는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final WebClient webClient;
    
    // TODO: 실제 user-couple-service URL로 변경 필요 (현재는 8081 포트 가정)
    @Value("${user-couple-service.url:http://user-couple-service:8081}")
    private String userCoupleServiceUrl;

    /**
     * 사용자 ID로 사용자 프로필 정보를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 사용자 프로필 정보
     */
    public UserProfile getUserProfile(String userId) {
        try {
            log.info("사용자 프로필 정보 조회 시작: userId={}, url={}", userId, userCoupleServiceUrl);
            
            UserProfile userProfile = webClient.get()
                    .uri(userCoupleServiceUrl + "/users/" + userId)
                    .retrieve()
                    .bodyToMono(UserProfile.class)
                    .block();
            
            if (userProfile != null) {
                log.info("사용자 프로필 정보 조회 성공: userId={}, gender={}", userId, userProfile.getGender());
                return userProfile;
            } else {
                log.warn("사용자 프로필 정보가 null입니다: userId={}", userId);
                return getDefaultUserProfile(userId);
            }
            
        } catch (WebClientResponseException.NotFound e) {
            log.warn("사용자를 찾을 수 없습니다: userId={}", userId);
            return getDefaultUserProfile(userId);
        } catch (WebClientResponseException e) {
            log.error("사용자 프로필 정보 조회 HTTP 오류: {} - {}", e.getStatusCode(), e.getMessage());
            return getDefaultUserProfile(userId);
        } catch (Exception e) {
            log.error("사용자 프로필 정보 조회 중 오류: {}", e.getMessage(), e);
            return getDefaultUserProfile(userId);
        }
    }

    /**
     * 커플 ID로 커플 프로필 정보를 조회합니다.
     * 
     * @param coupleId 커플 ID
     * @return 커플 프로필 정보
     */
    public CoupleProfile getCoupleProfile(String coupleId) {
        try {
            log.info("커플 프로필 정보 조회 시작: coupleId={}, url={}", coupleId, userCoupleServiceUrl);
            
            CoupleProfile coupleProfile = webClient.get()
                    .uri(userCoupleServiceUrl + "/couples/" + coupleId)
                    .retrieve()
                    .bodyToMono(CoupleProfile.class)
                    .block();
            
            if (coupleProfile != null) {
                log.info("커플 프로필 정보 조회 성공: coupleId={}", coupleId);
                return coupleProfile;
            } else {
                log.warn("커플 프로필 정보가 null입니다: coupleId={}", coupleId);
                return getDefaultCoupleProfile(coupleId);
            }
            
        } catch (WebClientResponseException.NotFound e) {
            log.warn("커플을 찾을 수 없습니다: coupleId={}", coupleId);
            return getDefaultCoupleProfile(coupleId);
        } catch (WebClientResponseException e) {
            log.error("커플 프로필 정보 조회 HTTP 오류: {} - {}", e.getStatusCode(), e.getMessage());
            return getDefaultCoupleProfile(coupleId);
        } catch (Exception e) {
            log.error("커플 프로필 정보 조회 중 오류: {}", e.getMessage(), e);
            return getDefaultCoupleProfile(coupleId);
        }
    }

    /**
     * 사용자 ID로 해당 사용자가 속한 커플 프로필 정보를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 커플 프로필 정보
     */
    public CoupleProfile getCoupleProfileByUserId(String userId) {
        try {
            log.info("사용자 ID로 커플 프로필 정보 조회 시작: userId={}, url={}", userId, userCoupleServiceUrl);
            
            CoupleProfile coupleProfile = webClient.get()
                    .uri(userCoupleServiceUrl + "/users/" + userId + "/couple")
                    .retrieve()
                    .bodyToMono(CoupleProfile.class)
                    .block();
            
            if (coupleProfile != null) {
                log.info("사용자 ID로 커플 프로필 정보 조회 성공: userId={}, coupleId={}", userId, coupleProfile.getCoupleId());
                return coupleProfile;
            } else {
                log.warn("사용자 ID로 커플 프로필 정보가 null입니다: userId={}", userId);
                return getDefaultCoupleProfileByUserId(userId);
            }
            
        } catch (WebClientResponseException.NotFound e) {
            log.warn("사용자가 속한 커플을 찾을 수 없습니다: userId={}", userId);
            return getDefaultCoupleProfileByUserId(userId);
        } catch (WebClientResponseException e) {
            log.error("사용자 ID로 커플 프로필 정보 조회 HTTP 오류: {} - {}", e.getStatusCode(), e.getMessage());
            return getDefaultCoupleProfileByUserId(userId);
        } catch (Exception e) {
            log.error("사용자 ID로 커플 프로필 정보 조회 중 오류: {}", e.getMessage(), e);
            return getDefaultCoupleProfileByUserId(userId);
        }
    }

    /**
     * 사용자 ID로 커플의 user1, user2 UserProfile만 반환합니다.
     * @param userId 사용자 ID
     * @return CoupleProfile (user1, user2 UserProfile만 포함)
     */
    public CoupleProfile getCoupleUserProfilesByUserId(String userId) {
        // 기존 getCoupleProfileByUserId 활용
        CoupleProfile coupleProfile = getCoupleProfileByUserId(userId);
        // 취향벡터 등은 포함하지 않으므로 그대로 반환
        return coupleProfile;
    }

    /**
     * 기본 사용자 프로필 정보를 반환합니다.
     * user-couple-service에 접근할 수 없을 때 사용됩니다.
     * 
     * @param userId 사용자 ID
     * @return 기본 사용자 프로필 정보
     */
    private UserProfile getDefaultUserProfile(String userId) {
        log.info("기본 사용자 프로필 정보 사용: userId={}", userId);
        return UserProfile.builder()
                .id(userId)
                .gender("M") // 기본값
                .birth("1990-01-01") // 기본 생년월일
                .build();
    }

    /**
     * 기본 커플 프로필 정보를 반환합니다.
     * user-couple-service에 접근할 수 없을 때 사용됩니다.
     * 
     * @param coupleId 커플 ID
     * @return 기본 커플 프로필 정보
     */
    private CoupleProfile getDefaultCoupleProfile(String coupleId) {
        log.info("기본 커플 프로필 정보 사용: coupleId={}", coupleId);
        return CoupleProfile.builder()
                .coupleId(coupleId)
                .user1(getDefaultUserProfile(coupleId + "_user1"))
                .user2(getDefaultUserProfile(coupleId + "_user2"))
                .build();
    }

    /**
     * 사용자 ID로 기본 커플 프로필 정보를 반환합니다.
     * user-couple-service에 접근할 수 없을 때 사용됩니다.
     * 
     * @param userId 사용자 ID
     * @return 기본 커플 프로필 정보
     */
    private CoupleProfile getDefaultCoupleProfileByUserId(String userId) {
        log.info("기본 커플 프로필 정보 사용: userId={}", userId);
        return CoupleProfile.builder()
                .coupleId(userId + "_couple")
                .user1(getDefaultUserProfile(userId + "_user1"))
                .user2(getDefaultUserProfile(userId + "_user2"))
                .build();
    }
} 