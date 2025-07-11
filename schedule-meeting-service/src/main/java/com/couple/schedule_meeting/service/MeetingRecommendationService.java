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

import java.util.ArrayList;
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
     * @return 생성된 TmpMeeting 문서
     */
    public TmpMeeting createMeetingRecommendation(MeetingCourseRecommendRequest request, String userId) {
        try {
            log.info("데이트 코스 추천 시작: userId={}", userId);
            
            // 1. 커플 취향 정보 조회 (기본 정보 + 취향 벡터)
            log.info("커플 취향 정보 조회 시작");
            CoupleInfo coupleInfo = userInfoService.getCoupleInfoByUserId(userId);

            UserInfo user1Info = coupleInfo.getUser1();
            UserInfo user2Info = coupleInfo.getUser2();
            
            // 2. RecommendationRequest 생성
            RecommendationRequest recommendationRequest = RecommendationRequest.builder()
                    .user1(RecommendationRequest.UserInfo.builder()
                            .gender(user1Info.getGender())
                            .preferences(user1Info.getPreferenceVector())
                            .build())
                    .user2(RecommendationRequest.UserInfo.builder()
                            .gender(user2Info.getGender())
                            .preferences(user2Info.getPreferenceVector())
                            .build())
                    .date(request.getDate().toString())
                    .weather(request.getWeather())
                    .startTime(request.getStartTime().toString())
                    .endTime(request.getEndTime().toString())
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
            
            // 4. 1순위 스토어들의 이름 추출
            log.info("1순위 스토어 이름 추출 시작");
            List<String> topStores = extractTopStores(recommendationResponse);
            log.info("1순위 스토어 추출 완료: {}개 스토어", topStores.size());
            
            // 5. 위도/경도 리스트 생성
            log.info("위도/경도 리스트 생성 시작");
            List<RecommendationService.LocationCoordinate> coordinates = recommendationService.getLocationCoordinates(recommendationResponse);
            
            if (coordinates == null || coordinates.isEmpty()) {
                log.error("위도/경도 리스트가 비어있습니다.");
                throw new RuntimeException("위도/경도 조회 실패");
            }
            
            log.info("위도/경도 리스트 생성 완료: {}개 장소", coordinates.size());
            
            // 6. 상세 경로 생성 (출발지 포함)
            log.info("상세 경로 생성 시작 (출발지 포함)");
            List<WaypointRouteRequest.LocationCoordinate> waypoints = new ArrayList<>();
            
            // 출발지 추가 (currentLat, currentLon이 제공된 경우)
            if (request.getCurrentLat() != null && request.getCurrentLon() != null) {
                waypoints.add(WaypointRouteRequest.LocationCoordinate.builder()
                        .name("출발지")
                        .lon(request.getCurrentLon())
                        .lat(request.getCurrentLat())
                        .build());
                log.info("출발지 추가: ({}, {})", request.getCurrentLat(), request.getCurrentLon());
            }
            
            // 추천된 장소들 추가 (null 좌표 제외)
            waypoints.addAll(coordinates.stream()
                    .filter(coord -> coord.getLongitude() != null && coord.getLatitude() != null)
                    .map(coord -> WaypointRouteRequest.LocationCoordinate.builder()
                            .name(coord.getStoreName())
                            .lon(coord.getLongitude().toString())
                            .lat(coord.getLatitude().toString())
                            .build())
                    .collect(Collectors.toList()));
            
            WaypointRouteRequest waypointRequest = WaypointRouteRequest.builder()
                    .waypoints(waypoints)
                    .routeType("fastest")
                    .build();
            
            WaypointRouteResponse routeResponse = waypointRouteService.getWaypointRoute(waypointRequest);
            
            if (routeResponse == null) {
                log.error("상세 경로 생성 실패");
                throw new RuntimeException("상세 경로 생성 실패");
            }
            
            log.info("상세 경로 생성 완료: {}개 segment (출발지 포함)", routeResponse.getSegments().size());
            
            // 7. TmpMeeting 문서 생성
            TmpMeeting.MeetingResults results = TmpMeeting.MeetingResults.builder()
                    .timeSlots(convertToTimeSlots(recommendationResponse.getTimeSlots()))
                    .routes(routeResponse) // routes는 별도 컬렉션에 저장하거나 여기에 포함
                    .build();
            
            TmpMeeting tmpMeeting = TmpMeeting.builder()
                    .name(request.getName())
                    .startTime(request.getStartTime().toString())
                    .endTime(request.getEndTime().toString())
                    .date(request.getDate().toString())
                    .keyword(request.getKeyword())
                    .weather(request.getWeather())
                    .currentLat(request.getCurrentLat())
                    .currentLon(request.getCurrentLon())
                    .results(results)
                    .stores(topStores) // 1순위 스토어들의 이름 추가
                    .build();
            
            // 8. MongoDB에 저장
            log.info("MongoDB 저장 시작");
            TmpMeeting savedMeeting = tmpMeetingRepository.save(tmpMeeting);
            
            log.info("데이트 코스 추천 완료: documentId={}", savedMeeting.getId());
            return savedMeeting;
            
        } catch (Exception e) {
            log.error("데이트 코스 추천 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("데이트 코스 추천 처리 실패", e);
        }
    }
    
    /**
     * RecommendationResponse에서 LLM이 선택한 장소들의 이름을 추출
     */
    private List<String> extractTopStores(RecommendationResponse recommendationResponse) {
        return recommendationResponse.getTimeSlots().stream()
                .filter(timeSlot -> timeSlot.getLlmRecommendation() != null && timeSlot.getLlmRecommendation().getSelected() != null)
                .map(timeSlot -> timeSlot.getLlmRecommendation().getSelected())
                .distinct()
                .collect(Collectors.toList());
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
                                        .similarity(null) // 외부 API에서 제공하지 않으므로 null로 설정
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