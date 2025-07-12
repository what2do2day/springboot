package com.couple.question_answer.controller;

import com.couple.common.dto.ApiResponse;
import com.couple.question_answer.dto.NotificationRequest;
import com.couple.question_answer.dto.NotificationResponse;
import com.couple.question_answer.entity.User;
import com.couple.question_answer.repository.UserRepository;
import com.couple.question_answer.service.FcmService;
import com.couple.question_answer.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final UserRepository userRepository;
    private final FcmService fcmService;
    private final NotificationService notificationService;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * 현재 시각에 알림을 받을 유저들을 조회
     */
    @GetMapping("/current-time-users")
    public ResponseEntity<List<User>> getCurrentTimeUsers() {
        String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
        List<User> users = userRepository.findUsersWithValidFcmTokenBySendTime(currentTime);
        
        log.info("Found {} users for current time: {}", users.size(), currentTime);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 특정 시각에 알림을 받을 유저들을 조회
     */
    @GetMapping("/users-by-time")
    public ResponseEntity<List<User>> getUsersByTime(@RequestParam String time) {
        List<User> users = userRepository.findUsersWithValidFcmTokenBySendTime(time);
        
        log.info("Found {} users for time: {}", users.size(), time);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 수동으로 알림 전송 테스트
     */
    @PostMapping("/send-test")
    public ResponseEntity<String> sendTestNotification(@RequestParam(required = false) String time) {
        String targetTime = time != null ? time : LocalDateTime.now().format(TIME_FORMATTER);
        
        List<User> users = userRepository.findUsersWithValidFcmTokenBySendTime(targetTime);
        
        if (users.isEmpty()) {
            return ResponseEntity.ok("No users found for time: " + targetTime);
        }
        
        int successCount = fcmService.sendQuestionNotification(users);
        
        return ResponseEntity.ok(String.format(
            "Test notification sent to %d/%d users for time: %s", 
            successCount, users.size(), targetTime
        ));
    }
    
    /**
     * 모든 유저 조회
     */
    @GetMapping("/all-users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    /**
     * 알림 정보 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody NotificationRequest request) {
        log.info("알림 생성 요청 - userId: {}, fcmToken: {}", userId, request.getFcmToken());

        UUID userIdUUID = UUID.fromString(userId);
        NotificationResponse response = notificationService.createNotification(userIdUUID, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("알림이 생성되었습니다.", response));
    }
} 