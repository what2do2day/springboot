package com.couple.question_answer.service;

import com.couple.question_answer.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FcmService {
    
    private final RestTemplate restTemplate;
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    
    public FcmService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * 단일 유저에게 Expo 푸시 알림 전송
     * @param user 알림을 받을 유저
     * @param title 알림 제목
     * @param body 알림 내용
     * @return 전송 성공 여부
     */
    public boolean sendNotificationToUser(User user, String title, String body) {
        if (user.getFcmToken() == null || user.getFcmToken().trim().isEmpty()) {
            log.warn("User {} has invalid FCM token", user.getId());
            return false;
        }
        
        return sendNotificationToToken(user.getFcmToken(), title, body);
    }
    
    /**
     * 여러 유저에게 일괄 Expo 푸시 알림 전송
     * @param users 알림을 받을 유저 목록
     * @param title 알림 제목
     * @param body 알림 내용
     * @return 성공적으로 전송된 알림 수
     */
    public int sendNotificationToUsers(List<User> users, String title, String body) {
        int successCount = 0;
        
        for (User user : users) {
            if (sendNotificationToUser(user, title, body)) {
                successCount++;
            }
        }
        
        log.info("Expo notification sent to {}/{} users successfully", successCount, users.size());
        return successCount;
    }
    
    /**
     * Expo 토큰으로 직접 푸시 알림 전송
     */
    public boolean sendNotificationToToken(String expoToken, String title, String body) {
        if (expoToken == null || expoToken.trim().isEmpty()) {
            log.warn("Invalid Expo token");
            return false;
        }
        
        try {
            // Expo 푸시 알림 요청 데이터 구성
            Map<String, Object> pushMessage = new HashMap<>();
            pushMessage.put("to", expoToken); // 원본 토큰 그대로 사용
            pushMessage.put("title", title);
            pushMessage.put("body", body);
            pushMessage.put("sound", "default");
            pushMessage.put("priority", "high");
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.set("Accept-encoding", "gzip, deflate");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(pushMessage, headers);
            
            // Expo 푸시 서비스로 요청 전송
            String response = restTemplate.postForObject(EXPO_PUSH_URL, request, String.class);
            
            log.info("Expo notification sent successfully to token {}: response={}", expoToken, response);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send Expo notification to token {}: {}", expoToken, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 질문 알림 전송
     * @param users 알림을 받을 유저 목록
     * @return 성공적으로 전송된 알림 수
     */
    public int sendQuestionNotification(List<User> users) {
        return sendNotificationToUsers(users, "오늘의 질문", "새로운 질문이 도착했습니다!");
    }
} 