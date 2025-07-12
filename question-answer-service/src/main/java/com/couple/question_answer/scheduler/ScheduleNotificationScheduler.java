package com.couple.question_answer.scheduler;

import com.couple.question_answer.entity.Notification;
import com.couple.question_answer.repository.NotificationRepository;
import com.couple.question_answer.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

//@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleNotificationScheduler {
    
    private final NotificationRepository notificationRepository;
    private final FcmService fcmService;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * 매 1분마다 notifications 테이블에서 send_time이 현재 시각과 일치하는 모든 notification의 fcmToken으로 푸시 알림 전송
     */
    @Scheduled(fixedRate = 60000)
    public void sendNotificationsFromTable() {
        try {
            String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
            log.info("[Scheduler] Checking notifications for time: {}", currentTime);
            
            List<Notification> notifications = notificationRepository.findBySendTime(currentTime);
            if (notifications.isEmpty()) {
                log.debug("No notifications to send at {}", currentTime);
                return;
            }
            
            int successCount = 0;
            for (Notification notification : notifications) {
                boolean sent = fcmService.sendNotificationToToken(
                    notification.getFcmToken(),
                    "오늘의 질문",
                    "새로운 질문이 도착했습니다!"
                );
                if (sent) successCount++;
            }
            log.info("[Scheduler] Sent {} notifications for time {}", successCount, currentTime);
        } catch (Exception e) {
            log.error("[Scheduler] Error while sending notifications", e);
        }
    }
} 