package com.couple.couple_chat.controller;

import com.couple.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/websocket")
@RequiredArgsConstructor
public class WebSocketController {

    // 활성 WebSocket 세션 저장소
    private static final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    
    // 사용자별 세션 매핑
    private static final Map<UUID, String> userSessionMap = new ConcurrentHashMap<>();

    /**
     * WebSocket 세션 등록 (WebSocket 핸들러에서 호출)
     */
    public static void registerSession(String sessionId, WebSocketSession session, UUID userId) {
        activeSessions.put(sessionId, session);
        userSessionMap.put(userId, sessionId);
        log.info("WebSocket 세션 등록: sessionId={}, userId={}", sessionId, userId);
    }

    /**
     * WebSocket 세션 해제 (WebSocket 핸들러에서 호출)
     */
    public static void unregisterSession(String sessionId) {
        WebSocketSession session = activeSessions.remove(sessionId);
        if (session != null) {
            // 사용자 매핑에서도 제거
            userSessionMap.entrySet().removeIf(entry -> entry.getValue().equals(sessionId));
            log.info("WebSocket 세션 해제: sessionId={}", sessionId);
        }
    }

    /**
     * 특정 사용자의 WebSocket 연결 해제
     */
    @PostMapping("/disconnect/user/{userId}")
    public ResponseEntity<ApiResponse<String>> disconnectUser(@PathVariable UUID userId) {
        try {
            String sessionId = userSessionMap.get(userId);
            if (sessionId != null) {
                WebSocketSession session = activeSessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    session.close();
                    log.info("사용자 WebSocket 연결 해제: userId={}, sessionId={}", userId, sessionId);
                    return ResponseEntity.ok(ApiResponse.success("사용자 연결이 해제되었습니다."));
                } else {
                    return ResponseEntity.ok(ApiResponse.success("이미 연결이 해제된 사용자입니다."));
                }
            } else {
                return ResponseEntity.ok(ApiResponse.success("연결된 세션이 없는 사용자입니다."));
            }
        } catch (Exception e) {
            log.error("사용자 연결 해제 중 오류: userId={}", userId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("연결 해제 중 오류가 발생했습니다."));
        }
    }

    /**
     * 특정 세션의 WebSocket 연결 해제
     */
    @PostMapping("/disconnect/session/{sessionId}")
    public ResponseEntity<ApiResponse<String>> disconnectSession(@PathVariable String sessionId) {
        try {
            WebSocketSession session = activeSessions.get(sessionId);
            if (session != null && session.isOpen()) {
                session.close();
                log.info("세션 WebSocket 연결 해제: sessionId={}", sessionId);
                return ResponseEntity.ok(ApiResponse.success("세션 연결이 해제되었습니다."));
            } else {
                return ResponseEntity.ok(ApiResponse.success("이미 연결이 해제된 세션입니다."));
            }
        } catch (Exception e) {
            log.error("세션 연결 해제 중 오류: sessionId={}", sessionId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("연결 해제 중 오류가 발생했습니다."));
        }
    }

    /**
     * 모든 WebSocket 연결 해제
     */
    @PostMapping("/disconnect/all")
    public ResponseEntity<ApiResponse<String>> disconnectAll() {
        try {
            int disconnectedCount = 0;
            for (WebSocketSession session : activeSessions.values()) {
                if (session.isOpen()) {
                    session.close();
                    disconnectedCount++;
                }
            }
            
            // 모든 세션 정보 초기화
            activeSessions.clear();
            userSessionMap.clear();
            
            log.info("모든 WebSocket 연결 해제: {}개 세션", disconnectedCount);
            return ResponseEntity.ok(ApiResponse.success(disconnectedCount + "개의 연결이 해제되었습니다."));
        } catch (Exception e) {
            log.error("모든 연결 해제 중 오류", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("연결 해제 중 오류가 발생했습니다."));
        }
    }

    /**
     * 활성 WebSocket 세션 맵 반환 (내부용)
     */
    public static Map<String, WebSocketSession> getActiveSessionsMap() {
        return activeSessions;
    }

    /**
     * 활성 WebSocket 연결 목록 조회
     */
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getActiveSessions() {
        try {
            Map<String, Object> sessionInfo = Map.of(
                "totalSessions", activeSessions.size(),
                "activeSessions", activeSessions.keySet(),
                "userSessions", userSessionMap
            );
            
            return ResponseEntity.ok(ApiResponse.success(sessionInfo));
        } catch (Exception e) {
            log.error("세션 목록 조회 중 오류", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("세션 목록 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 특정 사용자의 연결 상태 확인
     */
    @GetMapping("/status/user/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserConnectionStatus(@PathVariable UUID userId) {
        try {
            String sessionId = userSessionMap.get(userId);
            boolean isConnected = false;
            String status = "disconnected";
            
            if (sessionId != null) {
                WebSocketSession session = activeSessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    isConnected = true;
                    status = "connected";
                }
            }
            
            Map<String, Object> statusInfo = Map.of(
                "userId", userId.toString(),
                "isConnected", isConnected,
                "status", status,
                "sessionId", sessionId
            );
            
            return ResponseEntity.ok(ApiResponse.success(statusInfo));
        } catch (Exception e) {
            log.error("사용자 연결 상태 확인 중 오류: userId={}", userId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("연결 상태 확인 중 오류가 발생했습니다."));
        }
    }
} 