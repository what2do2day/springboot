package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.util.GeoToGridConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WeatherCardService {
    private static final String WEATHER_API_BASE_URL = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
    private static final int WEATHER_API_NUM_OF_ROWS = 1000;
    private static final int WEATHER_API_PAGE_NO = 1;
    private static final String WEATHER_API_DATA_TYPE = "JSON";
    private static final String WEATHER_API_BASE_TIME = "0500";

    @Value("${weather.api-key}")
    private String serviceKey;

    public List<WeatherCardResponse> getWeatherCards(float lat, float lon) throws Exception {
        GeoToGridConverter.Grid grid = GeoToGridConverter.convert(lat, lon);
        int nx = grid.x;
        int ny = grid.y;
        System.out.println("[DEBUG] 변환된 nx, ny = " + nx + ", " + ny);
        String baseDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = WEATHER_API_BASE_TIME;

        String urlStr = String.format(
                WEATHER_API_BASE_URL +
                        "?serviceKey=%s&numOfRows=%d&pageNo=%d&dataType=%s&base_date=%s&base_time=%s&nx=%d&ny=%d",
                URLEncoder.encode(serviceKey, "UTF-8"),
                WEATHER_API_NUM_OF_ROWS,
                WEATHER_API_PAGE_NO,
                WEATHER_API_DATA_TYPE,
                baseDate,
                baseTime,
                nx,
                ny);

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

        return result;
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

    public static class WeatherCardResponse {
        private final String date;
        private final String weather;

        public WeatherCardResponse(String date, String weather) {
            this.date = date;
            this.weather = weather;
        }

        public String getDate() {
            return date;
        }

        public String getWeather() {
            return weather;
        }
    }
} 