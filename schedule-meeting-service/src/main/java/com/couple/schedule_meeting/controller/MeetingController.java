package com.couple.schedule_meeting.controller;

import com.couple.common.dto.ApiResponse;
import com.couple.schedule_meeting.dto.MeetingResponse;
import com.couple.schedule_meeting.dto.MeetingRecommendResponse;
import com.couple.schedule_meeting.entity.TmpMeeting;
import com.couple.schedule_meeting.service.MeetingService;
import com.couple.schedule_meeting.service.WeatherCardService;
import com.couple.schedule_meeting.service.MeetingRecommendationService;
import com.couple.schedule_meeting.service.MeetingSaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import com.couple.schedule_meeting.dto.MeetingCourseRecommendRequest;
import com.couple.schedule_meeting.dto.MeetingSaveRequest;
import com.couple.schedule_meeting.dto.DirectionRequest;
import com.couple.schedule_meeting.dto.WaypointRouteResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingService meetingService;
    private final WeatherCardService weatherCardService;
    private final MeetingRecommendationService meetingRecommendationService;
    private final MeetingSaveService meetingSaveService;
    private final com.couple.schedule_meeting.repository.TmpMeetingRepository tmpMeetingRepository;
    private final com.couple.schedule_meeting.service.DirectionService directionService;

    @GetMapping("/weather-cards")
    public ResponseEntity<ApiResponse<List<WeatherCardService.WeatherCardResponse>>> getWeatherCards(@RequestParam float lat, @RequestParam float lon) throws Exception {
        try {
            List<WeatherCardService.WeatherCardResponse> result = weatherCardService.getWeatherCards(lat, lon);
            return ResponseEntity.ok(ApiResponse.success("날씨 카드 조회 성공", result));
        } catch (Exception e) {
            log.error("날씨 카드 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("날씨 카드 조회 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/recommend")
    public ResponseEntity<ApiResponse<MeetingRecommendResponse>> recommendCourse(
            @RequestBody MeetingCourseRecommendRequest request,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Couple-ID") String coupleId) {
        
        try {
            // 통합 서비스를 사용하여 데이트 코스 추천 및 저장
            TmpMeeting tmpMeeting = meetingRecommendationService.createMeetingRecommendation(request, userId);
            
            MeetingRecommendResponse response = MeetingRecommendResponse.builder()
                    .documentId(tmpMeeting.getId())
                    .message("데이트 코스 추천이 완료되었습니다.")
                    .value(tmpMeeting)
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success("데이트 코스 추천이 완료되었습니다.", response));
            
        } catch (Exception e) {
            log.error("데이트 코스 추천 처리 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("데이트 코스 추천 처리 중 오류가 발생했습니다."));
        }
    }
    
    @GetMapping("/recommend/{tmpMeetingId}")
    public ResponseEntity<ApiResponse<TmpMeeting>> getTmpMeeting(
            @PathVariable String tmpMeetingId) {
        
        try {
            TmpMeeting tmpMeeting = tmpMeetingRepository.findById(tmpMeetingId)
                    .orElse(null);
            
            if (tmpMeeting == null) {
                log.warn("TmpMeeting을 찾을 수 없습니다: {}", tmpMeetingId);
                return ResponseEntity.status(404).body(ApiResponse.error("TmpMeeting을 찾을 수 없습니다."));
            }
            
            log.info("TmpMeeting 조회 성공: tmpMeetingId={}", tmpMeetingId);
            return ResponseEntity.ok(ApiResponse.success("TmpMeeting 조회 성공", tmpMeeting));
            
        } catch (Exception e) {
            log.error("TmpMeeting 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("TmpMeeting 조회 중 오류가 발생했습니다."));
        }
    }
    
    @GetMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<MeetingResponse>> getMeeting(
            @PathVariable String meetingId,
            @RequestHeader("X-Couple-ID") String coupleId) {
        
        try {
            UUID meetingUuid = UUID.fromString(meetingId);
            UUID coupleUuid = UUID.fromString(coupleId);
            
            MeetingResponse response = meetingService.getMeetingById(meetingUuid, coupleUuid);
            return ResponseEntity.ok(ApiResponse.success("데이트 일정 조회 성공", response));
            
        } catch (IllegalArgumentException e) {
            log.error("잘못된 UUID 형식: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("잘못된 UUID 형식입니다."));
        } catch (Exception e) {
            log.error("데이트 일정 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("데이트 일정 조회 중 오류가 발생했습니다."));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> saveMeeting(
            @RequestBody MeetingSaveRequest request,
            @RequestHeader("X-Couple-ID") String coupleId) {
        
        try {
            // TmpMeeting 문서를 조회하여 Meeting, MeetingPlaces, MeetingKeywords, Route를 저장
            UUID meetingId = meetingSaveService.saveMeetingFromTmpMeeting(request.getTmpMeetingId(), UUID.fromString(coupleId));
            
            return ResponseEntity.ok(ApiResponse.success("데이트 일정이 성공적으로 저장되었습니다. Meeting ID: " + meetingId, meetingId.toString()));
            
        } catch (Exception e) {
            log.error("데이트 일정 저장 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("데이트 일정 저장 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/direction")
    public ResponseEntity<ApiResponse<WaypointRouteResponse>> getDirection(
            @RequestBody DirectionRequest request) {
        
        try {
            log.info("대중교통 경로 조회 요청: 현재위치({}, {}), 장소ID={}", 
                    request.getCurrentLat(), request.getCurrentLon(), request.getPlaceId());
            
            WaypointRouteResponse response = directionService.getDirection(request);
            
            if (response != null) {
                log.info("대중교통 경로 조회 성공");
                return ResponseEntity.ok(ApiResponse.success("대중교통 경로 조회 성공", response));
            } else {
                log.warn("대중교통 경로 조회 실패");
                return ResponseEntity.status(404).body(ApiResponse.error("대중교통 경로를 찾을 수 없습니다."));
            }
            
        } catch (Exception e) {
            log.error("대중교통 경로 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("대중교통 경로 조회 중 오류가 발생했습니다."));
        }
    }
    
    @PostMapping("/path")
    public ResponseEntity<ApiResponse<WaypointRouteResponse>> getPath(
            @RequestBody MeetingCourseRecommendRequest request,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Couple-ID") String coupleId) {
        
        try {
            log.info("대중교통 경로 조회 요청: 사용자={}, 커플={}", userId, coupleId);
            
            // TODO: 여기에 대중교통 경로 조회 로직 구현
            // 현재는 기존 direction API와 동일한 응답 형태로 반환
            WaypointRouteResponse response = directionService.getDirection(
                DirectionRequest.builder()
                    .currentLat("37.5665") // 서울 시청 좌표 (기본값)
                    .currentLon("126.9780")
                    .placeId("test_place_id")
                    .build()
            );
            
            if (response != null) {
                log.info("대중교통 경로 조회 성공");
                return ResponseEntity.ok(ApiResponse.success("대중교통 경로 조회 성공", response));
            } else {
                log.warn("대중교통 경로 조회 실패");
                return ResponseEntity.status(404).body(ApiResponse.error("대중교통 경로를 찾을 수 없습니다."));
            }
            
        } catch (Exception e) {
            log.error("대중교통 경로 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("대중교통 경로 조회 중 오류가 발생했습니다."));
        }
    }
} 