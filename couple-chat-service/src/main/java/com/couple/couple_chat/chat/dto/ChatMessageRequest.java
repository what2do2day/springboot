package com.couple.couple_chat.chat.dto;

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

    @NotBlank(message = "메시지는 필수입니다")
    private String message;

    @Builder.Default
    private String messageType = "TEXT"; // TEXT, IMAGE, FILE, EMOJI
} 