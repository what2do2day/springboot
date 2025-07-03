package com.couple.schedule_meeting.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MeetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("실제 외부 기상청 API를 호출하여 날씨 요약 카드를 조회한다.")
    void getWeatherCards_realApiCall() throws Exception {
        // 1986베이커 위도/경도 예시
        float lat = 37.66198378f;
        float lon = 127.0659982f;

        ResultActions result = mockMvc.perform(get("/api/meetings/weather-cards")
                .param("lat", String.valueOf(lat))
                .param("lon", String.valueOf(lon)));

//        result.andExpect(status().isOk());
        // 추가적으로 응답 body 검증을 원하면 .andExpect()로 jsonPath 등 사용 가능
    }
} 