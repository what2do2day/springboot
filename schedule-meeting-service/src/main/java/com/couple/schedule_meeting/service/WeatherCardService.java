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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WeatherCardService {
    private static final String WEATHER_API_BASE_URL = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
    private static final int WEATHER_API_NUM_OF_ROWS = 1000;
    private static final int WEATHER_API_PAGE_NO = 1;
    private static final String WEATHER_API_DATA_TYPE = "JSON";
    
    // 기상청 발표시각 (매일 02, 05, 08, 11, 14, 17, 20, 23시)
    private static final int[] WEATHER_API_BASE_HOURS = {2, 5, 8, 11, 14, 17, 20, 23};

    @Value("${weather.api-key}")
    private String serviceKey;

    public List<WeatherCardResponse> getWeatherCards(float lat, float lon) throws Exception {
        GeoToGridConverter.Grid grid = GeoToGridConverter.convert(lat, lon);
        int nx = grid.x;
        int ny = grid.y;
        System.out.println("[DEBUG] 변환된 nx, ny = " + nx + ", " + ny);
        
        // 현재 시간에 맞는 가장 최신의 발표시각 계산
        LocalDateTime koreaNow = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime baseDateTime = calculateBaseDateTime(koreaNow);
        String baseDate = baseDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = baseDateTime.format(DateTimeFormatter.ofPattern("HHmm"));
        
        System.out.println("[DEBUG] 한국 시간 기준: " + koreaNow);
        System.out.println("[DEBUG] API 호출 baseDate: " + baseDate);
        System.out.println("[DEBUG] API 호출 baseTime: " + baseTime);

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
            LocalDate koreaToday = LocalDate.now(ZoneId.of("Asia/Seoul"));
            
            if (itemDate.isAfter(koreaToday.plusDays(4)) || itemDate.isBefore(koreaToday)) {
                System.out.println("[DEBUG] 날짜 필터링 제외: " + itemDate);
                continue;
            }

            summaries.putIfAbsent(date, new DailySimpleSummary(date));
            summaries.get(date).update(category, value);
        }

        List<WeatherCardResponse> result = new ArrayList<>();
        summaries.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> result.add(new WeatherCardResponse(e.getKey(), e.getValue().classifyWeather())));

        return result;
    }

    /**
     * 현재 시간에 맞는 가장 최신의 기상청 발표시각을 계산합니다.
     * 기상청 발표시각: 02, 05, 08, 11, 14, 17, 20, 23시
     * 각 발표시각으로부터 +10시간 후에 해당 발표시각의 예보 데이터를 사용할 수 있습니다.
     */
    private LocalDateTime calculateBaseDateTime(LocalDateTime currentTime) {
        LocalDate currentDate = currentTime.toLocalDate();
        LocalTime currentTimeOfDay = currentTime.toLocalTime();
        
        // 현재 시간에서 10시간을 뺀 시간을 기준으로 계산
        LocalDateTime adjustedTime = currentTime.minusHours(10);
        
        // 가장 최근의 발표시각 찾기
        LocalDateTime baseDateTime = null;
        
        for (int i = WEATHER_API_BASE_HOURS.length - 1; i >= 0; i--) {
            int baseHour = WEATHER_API_BASE_HOURS[i];
            LocalDateTime candidateDateTime = LocalDateTime.of(currentDate, LocalTime.of(baseHour, 0));
            
            // 만약 오늘의 발표시각이 현재 시간보다 늦다면, 어제의 마지막 발표시각 사용
            if (candidateDateTime.isAfter(adjustedTime)) {
                if (i == 0) {
                    // 첫 번째 발표시각(02시)보다 이전이면 어제의 마지막 발표시각(23시) 사용
                    baseDateTime = LocalDateTime.of(currentDate.minusDays(1), LocalTime.of(23, 0));
                } else {
                    // 이전 발표시각 사용
                    baseDateTime = LocalDateTime.of(currentDate, LocalTime.of(WEATHER_API_BASE_HOURS[i-1], 0));
                }
            } else {
                baseDateTime = candidateDateTime;
                break;
            }
        }
        
        // 만약 여전히 null이라면 (현재 시간이 오늘 02시 이전인 경우), 어제 23시 사용
        if (baseDateTime == null) {
            baseDateTime = LocalDateTime.of(currentDate.minusDays(1), LocalTime.of(23, 0));
        }
        
        return baseDateTime;
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