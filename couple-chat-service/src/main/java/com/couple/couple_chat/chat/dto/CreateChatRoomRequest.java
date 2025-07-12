package com.couple.couple_chat.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRoomRequest {
    
    @NotNull(message = "커플 ID는 필수입니다")
    private UUID coupleId;
    
    @NotNull(message = "사용자1 ID는 필수입니다")
    private UUID user1Id;
    
    @NotNull(message = "사용자2 ID는 필수입니다")
    private UUID user2Id;
} 