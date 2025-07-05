package com.couple.schedule_meeting.controller;

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

    @GetMapping("/weather-cards")
    public ResponseEntity<List<WeatherCardService.WeatherCardResponse>> getWeatherCards(@RequestParam float lat, @RequestParam float lon) throws Exception {
        List<WeatherCardService.WeatherCardResponse> result = weatherCardService.getWeatherCards(lat, lon);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/path")
    public ResponseEntity<MeetingRecommendResponse> recommendCourse(
            @RequestBody MeetingCourseRecommendRequest request,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Couple-ID") String coupleId) {
        
        try {
            // 통합 서비스를 사용하여 데이트 코스 추천 및 저장
            Object tmpMeeting = meetingRecommendationService.createMeetingRecommendation(request, userId);
            
            MeetingRecommendResponse response = MeetingRecommendResponse.builder()
                    .documentId(tmpMeeting.toString())
                    .message("데이트 코스 추천이 완료되었습니다.")
                    .value(tmpMeeting)
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("데이트 코스 추천 처리 중 오류: {}", e.getMessage(), e);
            
            MeetingRecommendResponse errorResponse = MeetingRecommendResponse.builder()
                    .documentId(null)
                    .message("데이트 코스 추천 처리 중 오류가 발생했습니다.")
                    .value(null)
                    .build();
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/recommend/{tmpMeetingId}")
    public ResponseEntity<TmpMeeting> getTmpMeeting(
            @PathVariable String tmpMeetingId) {
        
        try {
            TmpMeeting tmpMeeting = tmpMeetingRepository.findById(tmpMeetingId)
                    .orElse(null);
            
            if (tmpMeeting == null) {
                log.warn("TmpMeeting을 찾을 수 없습니다: {}", tmpMeetingId);
                return ResponseEntity.notFound().build();
            }
            
            log.info("TmpMeeting 조회 성공: tmpMeetingId={}", tmpMeetingId);
            return ResponseEntity.ok(tmpMeeting);
            
        } catch (Exception e) {
            log.error("TmpMeeting 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{meetingId}")
    public ResponseEntity<MeetingResponse> getMeeting(
            @PathVariable String meetingId,
            @RequestHeader("X-Couple-ID") String coupleId) {
        
        try {
            UUID meetingUuid = UUID.fromString(meetingId);
            UUID coupleUuid = UUID.fromString(coupleId);
            
            MeetingResponse response = meetingService.getMeetingById(meetingUuid, coupleUuid);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("잘못된 UUID 형식: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("데이트 일정 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<String> saveMeeting(
            @RequestBody MeetingSaveRequest request,
            @RequestHeader("X-Couple-ID") String coupleId) {
        
        try {
            // TmpMeeting 문서를 조회하여 Meeting, MeetingPlaces, MeetingKeywords, Route를 저장
            UUID meetingId = meetingSaveService.saveMeetingFromTmpMeeting(request.getTmpMeetingId(), UUID.fromString(coupleId));
            
            return ResponseEntity.ok("데이트 일정이 성공적으로 저장되었습니다. Meeting ID: " + meetingId);
            
        } catch (Exception e) {
            log.error("데이트 일정 저장 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("데이트 일정 저장 중 오류가 발생했습니다.");
        }
    }
} 