package com.couple.schedule_meeting.controller;

import com.couple.schedule_meeting.service.WeatherCardService;
import com.couple.schedule_meeting.service.MeetingRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.couple.schedule_meeting.dto.MeetingCourseRecommendRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {
    private final WeatherCardService weatherCardService;
    private final MeetingRecommendationService meetingRecommendationService;

    @GetMapping("/weather-cards")
    public ResponseEntity<List<WeatherCardService.WeatherCardResponse>> getWeatherCards(@RequestParam float lat, @RequestParam float lon) throws Exception {
        List<WeatherCardService.WeatherCardResponse> result = weatherCardService.getWeatherCards(lat, lon);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/recommend")
    public ResponseEntity<String> recommendCourse(
            @RequestBody MeetingCourseRecommendRequest request,
            @RequestHeader("user_id") String userId,
            @RequestHeader("couple_id") String coupleId) {
        
        try {
            // 통합 서비스를 사용하여 데이트 코스 추천 및 저장
            String documentId = meetingRecommendationService.createMeetingRecommendation(request, userId, coupleId);
            
            return ResponseEntity.ok("데이트 코스 추천이 완료되었습니다. Document ID: " + documentId);
            
        } catch (Exception e) {
            log.error("데이트 코스 추천 처리 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("데이트 코스 추천 처리 중 오류가 발생했습니다.");
        }
    }
} 