package com.couple.couple_chat.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

    private UUID id;
    private UUID roomId;
    private UUID senderId;
    private String senderName;
    private String message;
    private String messageType;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
} 