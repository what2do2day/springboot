package com.couple.schedule_meeting.controller;

import com.couple.schedule_meeting.service.WeatherCardService;
import com.couple.schedule_meeting.service.RecommendationService;
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
import com.couple.schedule_meeting.dto.RecommendationRequest;
import com.couple.schedule_meeting.dto.RecommendationResponse;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {
    private final WeatherCardService weatherCardService;
    private final RecommendationService recommendationService;

    @GetMapping("/weather-cards")
    public ResponseEntity<List<WeatherCardService.WeatherCardResponse>> getWeatherCards(@RequestParam float lat, @RequestParam float lon) throws Exception {
        List<WeatherCardService.WeatherCardResponse> result = weatherCardService.getWeatherCards(lat, lon);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/recommend")
    public ResponseEntity<List<RecommendationService.LocationCoordinate>> recommendCourse(
            @RequestBody MeetingCourseRecommendRequest request,
            @RequestHeader("user_id") String userId,
            @RequestHeader("couple_id") String coupleId) {
        
        try {
            // 2. RecommendationRequest 생성
            RecommendationRequest recommendationRequest = RecommendationRequest.builder()
                    .user1(RecommendationRequest.UserInfo.builder()
                            .gender("M") // TODO: 실제 사용자 정보에서 가져오기
                            .vector(java.util.Map.of("key1", "value1", "key2", "value2")) // TODO: 실제 벡터 데이터
                            .build())
                    .user2(RecommendationRequest.UserInfo.builder()
                            .gender("F") // TODO: 실제 사용자 정보에서 가져오기
                            .vector(java.util.Map.of("key1", "value1", "key2", "value2")) // TODO: 실제 벡터 데이터
                            .build())
                    .date(request.getDate())
                    .weather(request.getWeather())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .keywords(request.getKeyword())
                    .build();
            
            // 3. 외부 추천 API 호출
            RecommendationResponse response = recommendationService.getRecommendations(recommendationRequest);
            
            // 4. 위도/경도 리스트 생성
            List<RecommendationService.LocationCoordinate> coordinates = recommendationService.getLocationCoordinates(response);
            
            return ResponseEntity.ok(coordinates);
            
        } catch (Exception e) {
            // TODO: 적절한 예외 처리
            return ResponseEntity.internalServerError().build();
        }
    }
} 