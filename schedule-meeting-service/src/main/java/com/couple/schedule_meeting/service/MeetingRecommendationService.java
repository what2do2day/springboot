package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.CoupleInfo;
import com.couple.schedule_meeting.dto.MeetingCourseRecommendRequest;
import com.couple.schedule_meeting.dto.RecommendationRequest;
import com.couple.schedule_meeting.dto.RecommendationResponse;
import com.couple.schedule_meeting.dto.UserInfo;
import com.couple.schedule_meeting.dto.WaypointRouteRequest;
import com.couple.schedule_meeting.dto.WaypointRouteResponse;
import com.couple.schedule_meeting.entity.TmpMeeting;
import com.couple.schedule_meeting.repository.TmpMeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 데이트 코스 추천 및 상세 경로 생성을 통합 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingRecommendationService {

    private final RecommendationService recommendationService;
    private final WaypointRouteService waypointRouteService;
    private final TmpMeetingRepository tmpMeetingRepository;
    private final UserInfoService userInfoService;

    /**
     * 데이트 코스 추천 요청을 처리하고 tmp_meetings 문서를 생성하여 저장합니다.
     * 
     * @param request 데이트 코스 추천 요청
     * @param userId 사용자 ID
     * @param coupleId 커플 ID
     * @return 생성된 TmpMeeting 문서 ID
     */
    public String createMeetingRecommendation(MeetingCourseRecommendRequest request, String userId, String coupleId) {
        try {
            log.info("데이트 코스 추천 시작: userId={}, coupleId={}", userId, coupleId);
            
            // 1. 커플 취향 정보 조회 (기본 정보 + 취향 벡터)
            log.info("커플 취향 정보 조회 시작");
                    CoupleInfo coupleInfo = userInfoService.getCoupleInfoByUserId(userId);

        UserInfo user1Info = coupleInfo.getUser1();
        UserInfo user2Info = coupleInfo.getUser2();
            
            // 2. RecommendationRequest 생성
            RecommendationRequest recommendationRequest = RecommendationRequest.builder()
                    .user1(RecommendationRequest.UserInfo.builder()
                            .gender(user1Info.getGender())
                            .vector(user1Info.getPreferenceVector())
                            .build())
                    .user2(RecommendationRequest.UserInfo.builder()
                            .gender(user2Info.getGender())
                            .vector(user2Info.getPreferenceVector())
                            .build())
                    .date(request.getDate())
                    .weather(request.getWeather())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .keywords(request.getKeyword())
                    .build();
            
            // 3. 외부 추천 API 호출
            log.info("외부 추천 API 호출 시작");
            RecommendationResponse recommendationResponse = recommendationService.getRecommendations(recommendationRequest);
            
            if (recommendationResponse == null || recommendationResponse.getTimeSlots() == null) {
                log.error("추천 API 응답이 null이거나 timeSlots가 없습니다.");
                throw new RuntimeException("추천 API 응답 오류");
            }
            
            log.info("추천 API 응답 성공: {}개 timeSlot", recommendationResponse.getTimeSlots().size());
            
            // 4. 위도/경도 리스트 생성
            log.info("위도/경도 리스트 생성 시작");
            List<RecommendationService.LocationCoordinate> coordinates = recommendationService.getLocationCoordinates(recommendationResponse);
            
            if (coordinates == null || coordinates.isEmpty()) {
                log.error("위도/경도 리스트가 비어있습니다.");
                throw new RuntimeException("위도/경도 조회 실패");
            }
            
            log.info("위도/경도 리스트 생성 완료: {}개 장소", coordinates.size());
            
            // 5. 상세 경로 생성
            log.info("상세 경로 생성 시작");
            WaypointRouteRequest waypointRequest = WaypointRouteRequest.builder()
                    .waypoints(coordinates.stream()
                            .map(coord -> WaypointRouteRequest.LocationCoordinate.builder()
                                    .name(coord.getStoreName())
                                    .lon(coord.getLongitude().toString())
                                    .lat(coord.getLatitude().toString())
                                    .build())
                            .collect(Collectors.toList()))
                    .routeType("fastest")
                    .build();
            
            WaypointRouteResponse routeResponse = waypointRouteService.getWaypointRoute(waypointRequest);
            
            if (routeResponse == null) {
                log.error("상세 경로 생성 실패");
                throw new RuntimeException("상세 경로 생성 실패");
            }
            
            log.info("상세 경로 생성 완료: {}개 segment", routeResponse.getSegments().size());
            
            // 6. TmpMeeting 문서 생성
            TmpMeeting.MeetingResults results = TmpMeeting.MeetingResults.builder()
                    .timeSlots(convertToTimeSlots(recommendationResponse.getTimeSlots()))
                    .routes(routeResponse) // routes는 별도 컬렉션에 저장하거나 여기에 포함
                    .build();
            
            TmpMeeting tmpMeeting = TmpMeeting.builder()
                    .name(request.getName())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .date(request.getDate())
                    .keyword(request.getKeyword())
                    .results(results)
                    .build();
            
            // 7. MongoDB에 저장
            log.info("MongoDB 저장 시작");
            TmpMeeting savedMeeting = tmpMeetingRepository.save(tmpMeeting);
            
            log.info("데이트 코스 추천 완료: documentId={}", savedMeeting.getId());
            return savedMeeting.getId();
            
        } catch (Exception e) {
            log.error("데이트 코스 추천 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("데이트 코스 추천 처리 실패", e);
        }
    }
    
    /**
     * RecommendationResponse의 TimeSlot을 TmpMeeting의 TimeSlot으로 변환
     */
    private List<TmpMeeting.TimeSlot> convertToTimeSlots(List<RecommendationResponse.TimeSlot> responseTimeSlots) {
        return responseTimeSlots.stream()
                .map(responseSlot -> TmpMeeting.TimeSlot.builder()
                        .slot(responseSlot.getSlot())
                        .topCandidates(responseSlot.getTopCandidates().stream()
                                .map(candidate -> TmpMeeting.StoreCandidate.builder()
                                        .storeName(candidate.getStoreName())
                                        .score(candidate.getScore())
                                        .similarity(candidate.getSimilarity())
                                        .description(candidate.getDescription())
                                        .build())
                                .collect(Collectors.toList()))
                        .llmRecommendation(TmpMeeting.LlmRecommendation.builder()
                                .selected(responseSlot.getLlmRecommendation().getSelected())
                                .reason(responseSlot.getLlmRecommendation().getReason())
                                .build())
                        .build())
                .collect(Collectors.toList());
    }
} 