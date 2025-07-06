package com.couple.couple_chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {

    @NotNull(message = "방 ID는 필수입니다")
    private UUID roomId;

    @NotBlank(message = "메시지는 필수입니다")
    private String message;

    @Builder.Default
    private String messageType = "TEXT"; // TEXT, IMAGE, FILE, EMOJI
} 