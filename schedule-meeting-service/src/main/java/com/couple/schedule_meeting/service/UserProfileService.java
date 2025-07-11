package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.CoupleProfile;
import com.couple.schedule_meeting.dto.UserProfile;
import com.couple.schedule_meeting.dto.CoupleMemberResponse;
import com.couple.schedule_meeting.dto.CoupleInfoResponse;
import com.couple.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * user-couple-service에서 사용자 프로필 정보를 가져오는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final WebClient webClient;
    
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
                    .uri(userCoupleServiceUrl + "/api/couples/members/")
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
            
            List<CoupleMemberResponse> members = webClient.get()
                    .uri(userCoupleServiceUrl + "/api/couples/members")
                    .header("X-User-ID", userId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<CoupleMemberResponse>>() {})
                    .block();
            
            if (members != null && !members.isEmpty()) {
                // 첫 번째 멤버의 coupleId를 사용
                String coupleId = members.get(0).getCoupleId().toString();
                
                // 두 명의 사용자 정보를 UserProfile로 변환
                UserProfile user1 = null;
                UserProfile user2 = null;
                
                for (CoupleMemberResponse member : members) {
                    UserProfile userProfile = UserProfile.builder()
                            .id(member.getUserId().toString())
                            .gender(member.getGender())
                            .birth(member.getBirth())
                            .build();
                    
                    if (user1 == null) {
                        user1 = userProfile;
                    } else {
                        user2 = userProfile;
                        break;
                    }
                }
                
                CoupleProfile coupleProfile = CoupleProfile.builder()
                        .coupleId(coupleId)
                        .user1(user1)
                        .user2(user2)
                        .build();
                
                log.info("사용자 ID로 커플 프로필 정보 조회 성공: userId={}, coupleId={}", userId, coupleId);
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

    /**
     * 사용자 ID로 커플 정보와 디데이를 조회합니다.
     * 새로운 /api/couples/info API를 사용합니다.
     * 
     * @param userId 사용자 ID
     * @return 커플 정보와 디데이
     */
    public CoupleInfoResponse getCoupleInfoByUserId(String userId) {
        try {
            log.info("커플 정보 조회 시작: userId={}, url={}", userId, userCoupleServiceUrl);
            
            ApiResponse<CoupleInfoResponse> apiResponse = webClient.get()
                    .uri(userCoupleServiceUrl + "/api/couples/info")
                    .header("X-User-ID", userId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<CoupleInfoResponse>>() {})
                    .block();
            
            CoupleInfoResponse coupleInfo = apiResponse != null ? apiResponse.getData() : null;
            
            // 디버깅을 위해 JSON으로 변환해서 로그 출력
            if (coupleInfo != null) {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(coupleInfo);
                log.info("역직렬화된 CoupleInfoResponse JSON: {}", json);
            }
            
            if (coupleInfo != null && coupleInfo.getCoupleId() != null) {
                log.info("커플 정보 조회 성공: userId={}, coupleId={}, dday={}", 
                        userId, coupleInfo.getCoupleId(), coupleInfo.getDday());
                return coupleInfo;
            } else {
                log.error("커플 정보가 null이거나 coupleId가 null입니다: userId={}", userId);
                throw new RuntimeException("커플 정보를 찾을 수 없습니다: " + userId);
            }
            
        } catch (WebClientResponseException.NotFound e) {
            log.error("커플을 찾을 수 없습니다: userId={}, status={}", userId, e.getStatusCode());
            throw new RuntimeException("커플을 찾을 수 없습니다: " + userId, e);
        } catch (WebClientResponseException e) {
            log.error("커플 정보 조회 HTTP 오류: {} - {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("커플 정보 조회 중 HTTP 오류: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("커플 정보 조회 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("커플 정보 조회 중 오류: " + e.getMessage(), e);
        }
    }

    /**
     * 기본 커플 정보를 반환합니다.
     * user-couple-service에 접근할 수 없을 때 사용됩니다.
     * 
     * @param userId 사용자 ID
     * @return 기본 커플 정보
     */
    private CoupleInfoResponse getDefaultCoupleInfoResponse(String userId) {
        log.info("기본 커플 정보 사용: userId={}", userId);
        return CoupleInfoResponse.builder()
                .coupleId(UUID.fromString(userId + "_couple"))
                .coupleName("기본 커플")
                .dday(0L)
                .startDate(LocalDateTime.now().toString()) // 기본 시작일을 현재 시간으로 설정
                .user1(CoupleInfoResponse.UserInfo.builder()
                        .userId(UUID.fromString(userId + "_user1"))
                        .name("사용자1")
                        .gender("M")
                        .birth("1990-01-01")
                        .build())
                .user2(CoupleInfoResponse.UserInfo.builder()
                        .userId(UUID.fromString(userId + "_user2"))
                        .name("사용자2")
                        .gender("F")
                        .birth("1990-01-01")
                        .build())
                .build();
    }

    /**
     * 커플 ID로 커플 정보와 디데이를 조회합니다.
     * 새로운 /api/couples/info/{coupleId} API를 사용합니다.
     * 
     * @param coupleId 커플 ID
     * @return 커플 정보와 디데이
     */
    public CoupleInfoResponse getCoupleInfoByCoupleId(String coupleId) {
        try {
            log.info("커플 정보 조회 시작: coupleId={}, url={}", coupleId, userCoupleServiceUrl);
            
            CoupleInfoResponse coupleInfo = webClient.get()
                    .uri(userCoupleServiceUrl + "/api/couples/info/" + coupleId)
                    .retrieve()
                    .bodyToMono(CoupleInfoResponse.class)
                    .block();
            
            if (coupleInfo != null) {
                log.info("커플 정보 조회 성공: coupleId={}, dday={}", coupleId, coupleInfo.getDday());
                return coupleInfo;
            } else {
                log.warn("커플 정보가 null입니다: coupleId={}", coupleId);
                return getDefaultCoupleInfoResponse(coupleId + "_user");
            }
            
        } catch (WebClientResponseException.NotFound e) {
            log.warn("커플을 찾을 수 없습니다: coupleId={}", coupleId);
            return getDefaultCoupleInfoResponse(coupleId + "_user");
        } catch (WebClientResponseException e) {
            log.error("커플 정보 조회 HTTP 오류: {} - {}", e.getStatusCode(), e.getMessage());
            return getDefaultCoupleInfoResponse(coupleId + "_user");
        } catch (Exception e) {
            log.error("커플 정보 조회 중 오류: {}", e.getMessage(), e);
            return getDefaultCoupleInfoResponse(coupleId + "_user");
        }
    }
} 