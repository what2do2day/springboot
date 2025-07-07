package com.couple.question_answer.service;

import com.couple.question_answer.dto.NotificationRequest;
import com.couple.question_answer.dto.NotificationResponse;
import com.couple.question_answer.entity.Notification;
import com.couple.question_answer.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationResponse createNotification(UUID userId, NotificationRequest request) {
        log.info("알림 생성 요청 - userId: {}, fcmToken: {}", userId, request.getFcmToken());

        Notification notification = Notification.builder()
                .userId(userId)
                .fcmToken(request.getFcmToken())
                .sendTime(request.getSendTime())
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("알림 생성 완료 - notificationId: {}", savedNotification.getId());

        return convertToResponse(savedNotification);
    }

    private NotificationResponse convertToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .fcmToken(notification.getFcmToken())
                .createdAt(notification.getCreatedAt())
                .sendTime(notification.getSendTime())
                .build();
    }
} 