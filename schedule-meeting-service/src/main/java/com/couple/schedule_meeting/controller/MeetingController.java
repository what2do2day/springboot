package com.couple.schedule_meeting.controller;

import com.couple.schedule_meeting.service.WeatherCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {
    private final WeatherCardService weatherCardService;

    @GetMapping("/weather-cards")
    public ResponseEntity<List<WeatherCardService.WeatherCardResponse>> getWeatherCards(@RequestParam float lat, @RequestParam float lon) throws Exception {
        List<WeatherCardService.WeatherCardResponse> result = weatherCardService.getWeatherCards(lat, lon);
        return ResponseEntity.ok(result);
    }
} 