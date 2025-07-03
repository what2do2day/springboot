package com.couple.schedule_meeting.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.couple.schedule_meeting.util.GeoToGridConverter;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {
    @Value("${weather.api-key}")
    private String serviceKey;

    @GetMapping("/weather-cards")
    public ResponseEntity<List<WeatherCardResponse>> getWeatherCards(@RequestParam float lat, @RequestParam float lon) throws Exception {
        GeoToGridConverter.Grid grid = GeoToGridConverter.convert(lat, lon);
        int nx = grid.x;
        int ny = grid.y;
        System.out.println("[DEBUG] 변환된 nx, ny = " + nx + ", " + ny);
        String baseDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = "0500";

        String urlStr = String.format(
                "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst" +
                        "?serviceKey=%s&numOfRows=1000&pageNo=1&dataType=JSON&base_date=%s&base_time=%s&nx=%d&ny=%d",
                URLEncoder.encode(serviceKey, "UTF-8"), baseDate, baseTime, nx, ny);

        String json = fetchJson(urlStr);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        JsonNode items = root.path("response").path("body").path("items").path("item");

        Map<String, DailySimpleSummary> summaries = new HashMap<>();
        for (JsonNode item : items) {
            String date = item.get("fcstDate").asText();
            String category = item.get("category").asText();
            String value = item.get("fcstValue").asText();

            LocalDate itemDate = LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
            if (itemDate.isAfter(LocalDate.now().plusDays(4)) || itemDate.isBefore(LocalDate.now())) continue;

            summaries.putIfAbsent(date, new DailySimpleSummary(date));
            summaries.get(date).update(category, value);
        }

        List<WeatherCardResponse> result = new ArrayList<>();
        summaries.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> result.add(new WeatherCardResponse(e.getKey(), e.getValue().classifyWeather())));

        return ResponseEntity.ok(result);
    }

    private String fetchJson(String urlStr) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    static class DailySimpleSummary {
        String date;
        boolean hasRain = false;
        boolean hasSnow = false;
        boolean hasCloud = false;
        boolean hasOvercast = false;

        public DailySimpleSummary(String date) {
            this.date = date;
        }

        void update(String category, String value) {
            switch (category) {
                case "PTY":
                    switch (value) {
                        case "1": case "2": case "4":
                            hasRain = true;
                            break;
                        case "3":
                            hasSnow = true;
                            break;
                    }
                    break;
                case "SKY":
                    switch (value) {
                        case "3": hasCloud = true; break;
                        case "4": hasOvercast = true; break;
                    }
                    break;
            }
        }

        String classifyWeather() {
            if (hasSnow) return "눈";
            if (hasRain) return "비";
            if (hasOvercast) return "흐림";
            if (hasCloud) return "구름 조금";
            return "맑음";
        }
    }

    record WeatherCardResponse(String date, String weather) {
    }
} 