package com.couple.couple_chat.location.dto;

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
public class LocationShareResponse {

    private UUID id;
    private UUID roomId;
    private UUID senderId;
    private String senderName;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private String messageType;
    private LocalDateTime createdAt;
} 