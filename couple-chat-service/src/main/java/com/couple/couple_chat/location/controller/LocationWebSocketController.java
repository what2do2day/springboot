package com.couple.couple_chat.location.controller;

import com.couple.couple_chat.location.dto.LocationShareRequest;
import com.couple.couple_chat.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LocationWebSocketController {

    private final LocationService locationService;

    /**
     * WebSocket을 통한 위치 공유
     */
    @MessageMapping("/share-location")
    public void shareLocation(@Payload LocationShareRequest request) {
        // 실제로는 인증된 사용자 ID를 가져와야 함
        UUID senderId = UUID.randomUUID(); // 임시
        locationService.shareLocation(senderId, request);
    }
} 