package com.couple.couple_chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.couple.couple_chat.controller.WebSocketController;
import com.couple.common.security.JwtTokenProvider;
import com.couple.couple_chat.location.dto.LocationShareRequest;
import com.couple.couple_chat.location.dto.LocationShareResponse;
import com.couple.couple_chat.location.service.LocationService;

import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;

@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final LocationService locationService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("WebSocket 핸들러 등록 시작");
        
        // 표준 WebSocket 엔드포인트 (모든 클라이언트용)
        registry.addHandler(standardWebSocketHandler(), "/ws/couple-chat")
                .setAllowedOriginPatterns("*");
        log.info("WebSocket 핸들러 등록: /ws/couple-chat");
        
        // 게이트웨이를 통한 연결용 (connect 경로)
        registry.addHandler(standardWebSocketHandler(), "/ws/connect")
                .setAllowedOriginPatterns("*");
        log.info("WebSocket 핸들러 등록: /ws/connect");
        
        log.info("WebSocket 핸들러 등록 완료");
    }

    @Bean
    public WebSocketHandler standardWebSocketHandler() {
        return new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                log.info("표준 WebSocket 연결됨: {}", session.getId());
                
                // 사용자 ID 추출 (헤더에서)
                UUID userId = extractUserIdFromSession(session);
                
                // 세션 등록
                WebSocketController.registerSession(session.getId(), session, userId);
                
                // 연결 성공 메시지 전송
                String welcomeMessage = objectMapper.writeValueAsString(Map.of(
                    "type", "connection.established",
                    "message", "WebSocket 연결이 성공했습니다.",
                    "sessionId", session.getId(),
                    "userId", userId.toString()
                ));
                session.sendMessage(new TextMessage(welcomeMessage));
            }

            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                log.info("메시지 수신: {}", message.getPayload());
                
                try {
                    // JSON 메시지 파싱
                    Map<String, Object> messageData = objectMapper.readValue(message.getPayload(), Map.class);
                    log.info("파싱된 메시지 데이터: {}", messageData);
                    
                    // messageType 또는 type 필드에서 메시지 타입 추출
                    String messageType = (String) messageData.get("type");
                    if (messageType == null) {
                        messageType = (String) messageData.get("messageType");
                    }
                    
                    log.info("추출된 메시지 타입: {}", messageType);
                    
                    // 메시지 타입별 처리
                    if (messageType != null) {
                        switch (messageType) {
                            case "chat.message":
                                handleChatMessage(session, messageData);
                                break;
                            case "location.share":
                            case "LOCATION":
                                handleLocationMessage(session, messageData);
                                break;
                            case "ping":
                                handlePing(session);
                                break;
                            default:
                                // 에코 응답 (기본)
                                Map<String, Object> response = Map.of(
                                    "type", "message.echo",
                                    "originalMessage", messageData,
                                    "timestamp", System.currentTimeMillis()
                                );
                                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                        }
                    } else {
                        // messageType이 없는 경우 기본 에코 응답
                        Map<String, Object> response = Map.of(
                            "type", "message.echo",
                            "originalMessage", messageData,
                            "timestamp", System.currentTimeMillis()
                        );
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                    }
                    
                } catch (Exception e) {
                    log.error("메시지 처리 오류: {}", e.getMessage());
                    Map<String, Object> errorResponse = Map.of(
                        "type", "error",
                        "message", "메시지 처리 중 오류가 발생했습니다.",
                        "error", e.getMessage()
                    );
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
                }
            }
            
            private void handleChatMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
                // 채팅 메시지 처리 로직
                Map<String, Object> response = Map.of(
                    "type", "chat.message.received",
                    "messageId", messageData.get("messageId"),
                    "content", messageData.get("content"),
                    "timestamp", System.currentTimeMillis()
                );
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            }
            
            private void handleLocationMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
                // 위치 공유 메시지 처리 로직
                log.info("위치 공유 메시지 처리: {}", messageData);
                
                // 사용자 ID 추출
                UUID senderId = extractUserIdFromSession(session);
                log.info("위치 공유 요청 사용자: {}", senderId);
                
                // coupleId 추출 (roomId가 있으면 coupleId로 사용)
                String coupleIdStr = (String) messageData.get("coupleId");
                if (coupleIdStr == null) {
                    coupleIdStr = (String) messageData.get("roomId");
                }
                
                if (coupleIdStr == null) {
                    log.error("coupleId 또는 roomId가 없습니다");
                    Map<String, Object> errorResponse = Map.of(
                        "type", "location.share.error",
                        "error", "coupleId 또는 roomId가 필요합니다",
                        "timestamp", System.currentTimeMillis()
                    );
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
                    return;
                }
                
                UUID coupleId = UUID.fromString(coupleIdStr);
                
                // LocationService를 통한 위치 공유 처리
                try {
                    LocationShareRequest request = LocationShareRequest.builder()
                        .roomId(coupleId)
                        .latitude((Double) messageData.get("latitude"))
                        .longitude((Double) messageData.get("longitude"))
                        .build();
                    
                    LocationShareResponse locationResponse = locationService.shareLocation(senderId, request);
                    
                    // 성공 응답 (senderId 포함) - null 체크 추가
                    Map<String, Object> response = Map.of(
                        "type", "location.share.received",
                        "senderId", senderId.toString(),
                        "coupleId", coupleId.toString(),
                        "latitude", locationResponse.getLatitude(),
                        "longitude", locationResponse.getLongitude(),
                        "timestamp", locationResponse.getTimestamp() != null ? locationResponse.getTimestamp().toString() : LocalDateTime.now().toString(),
                        "messageType", "LOCATION"
                    );
                    
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                    
                    log.info("위치 공유 성공: senderId={}, coupleId={}", senderId, coupleId);
                    
                } catch (Exception e) {
                    log.error("위치 공유 처리 중 오류: {}", e.getMessage(), e);
                    Map<String, Object> errorResponse = Map.of(
                        "type", "location.share.error",
                        "error", "위치 공유 처리 중 오류가 발생했습니다: " + e.getMessage(),
                        "timestamp", System.currentTimeMillis()
                    );
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
                }
            }
            
            // 커플의 다른 사용자에게 위치 정보 브로드캐스트
            private void broadcastLocationToCouple(UUID coupleId, UUID senderId, LocationShareResponse locationResponse, WebSocketSession currentSession) {
                try {
                    // 커플의 다른 사용자 찾기 (실제로는 사용자 서비스에서 조회해야 함)
                    // 여기서는 간단히 모든 세션에 브로드캐스트
                    Map<String, Object> broadcastMessage = Map.of(
                        "type", "location.share.broadcast",
                        "senderId", senderId.toString(),
                        "coupleId", coupleId.toString(),
                        "latitude", locationResponse.getLatitude(),
                        "longitude", locationResponse.getLongitude(),
                        "timestamp", locationResponse.getTimestamp() != null ? locationResponse.getTimestamp().toString() : LocalDateTime.now().toString(),
                        "messageType", "LOCATION"
                    );
                    
                    String messageJson = objectMapper.writeValueAsString(broadcastMessage);
                    
                    // 모든 활성 세션에 브로드캐스트 (실제로는 커플 멤버만)
                    Map<String, WebSocketSession> activeSessions = WebSocketController.getActiveSessionsMap();
                    int broadcastCount = 0;
                    String currentSessionId = currentSession.getId();
                    
                    log.info("브로드캐스트 시작: 현재 세션 ID={}, 전체 세션 수={}", currentSessionId, activeSessions.size());
                    
                    for (Map.Entry<String, WebSocketSession> entry : activeSessions.entrySet()) {
                        String sessionId = entry.getKey();
                        WebSocketSession activeSession = entry.getValue();
                        
                        // 자신의 세션은 제외
                        if (sessionId.equals(currentSessionId)) {
                            log.info("자신의 세션 제외: {}", sessionId);
                            continue;
                        }
                        
                        if (activeSession.isOpen()) {
                            try {
                                activeSession.sendMessage(new TextMessage(messageJson));
                                broadcastCount++;
                                log.info("위치 정보 브로드캐스트 성공: sessionId={}", sessionId);
                            } catch (Exception e) {
                                log.error("브로드캐스트 전송 실패: sessionId={}", sessionId, e);
                            }
                        } else {
                            log.info("세션이 닫혀있음: {}", sessionId);
                        }
                    }
                    
                    log.info("위치 정보 브로드캐스트 완료: coupleId={}, senderId={}, 전송된 세션 수={}", 
                            coupleId, senderId, broadcastCount);
                    
                } catch (Exception e) {
                    log.error("위치 정보 브로드캐스트 중 오류", e);
                }
            }
            
            private void handlePing(WebSocketSession session) throws Exception {
                // Ping-Pong 응답
                Map<String, Object> response = Map.of(
                    "type", "pong",
                    "timestamp", System.currentTimeMillis()
                );
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    @Override
            public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
                log.info("WebSocket 연결 종료: {} - {}", session.getId(), status);
                // 세션 해제
                WebSocketController.unregisterSession(session.getId());
            }
            
            // 사용자 ID 추출 메서드
            private UUID extractUserIdFromSession(WebSocketSession session) {
                try {
                    // 헤더에서 사용자 ID 추출 (게이트웨이에서 추가된 헤더)
                    String userIdStr = session.getHandshakeHeaders().getFirst("X-User-ID");
                    if (userIdStr != null) {
                        return UUID.fromString(userIdStr);
                    }
                    
                    // 쿼리 파라미터에서 토큰 추출 시도
                    String query = session.getUri().getQuery();
                    if (query != null && query.contains("token=")) {
                        String token = query.split("token=")[1].split("&")[0];
                        log.info("토큰에서 사용자 ID 추출 시도: {}", token.substring(0, 20) + "...");
                        
                        // JWT 토큰 검증 및 사용자 ID 추출
                        if (jwtTokenProvider.validateToken(token)) {
                            UUID userId = jwtTokenProvider.getUserIdFromToken(token);
                            log.info("JWT 토큰에서 사용자 ID 추출 성공: {}", userId);
                            return userId;
                        } else {
                            log.warn("JWT 토큰 검증 실패");
                        }
                    }
                } catch (Exception e) {
                    log.warn("사용자 ID 추출 실패: {}", e.getMessage());
                }
                // 기본값으로 랜덤 UUID 반환
                return UUID.randomUUID();
            }
        };
    }
} 