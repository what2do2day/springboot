package com.couple.couple_chat.controller;

import com.couple.couple_chat.dto.ChatMessageRequest;
import com.couple.couple_chat.dto.ChatMessageResponse;
import com.couple.couple_chat.entity.CoupleChatRoom;
import com.couple.couple_chat.service.CoupleChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/couple-chat")
@RequiredArgsConstructor
public class CoupleChatController {

    private final CoupleChatService coupleChatService;

    /**
     * WebSocket을 통한 메시지 전송
     */
    @MessageMapping("/send-message")
    public void sendMessage(@Payload ChatMessageRequest request) {
        // 실제로는 인증된 사용자 ID를 가져와야 함
        UUID senderId = UUID.randomUUID(); // 임시
        coupleChatService.sendMessage(senderId, request);
    }

    /**
     * REST API를 통한 메시지 전송
     */
    @PostMapping("/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @RequestHeader("X-User-ID") UUID senderId,
            @Valid @RequestBody ChatMessageRequest request) {
        
        ChatMessageResponse response = coupleChatService.sendMessage(senderId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 채팅방 메시지 조회
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getChatMessages(
            @PathVariable UUID roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        List<ChatMessageResponse> messages = coupleChatService.getChatMessages(roomId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * 사용자의 채팅방 조회
     */
    @GetMapping("/rooms/my")
    public ResponseEntity<CoupleChatRoom> getMyChatRoom(@RequestHeader("X-User-ID") UUID userId) {
        CoupleChatRoom room = coupleChatService.getUserChatRoom(userId);
        return ResponseEntity.ok(room);
    }

    /**
     * 메시지 읽음 처리
     */
    @PutMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable UUID roomId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        coupleChatService.markMessagesAsRead(roomId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 읽지 않은 메시지 수 조회
     */
    @GetMapping("/rooms/{roomId}/unread-count")
    public ResponseEntity<Long> getUnreadMessageCount(
            @PathVariable UUID roomId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        Long count = coupleChatService.getUnreadMessageCount(roomId, userId);
        return ResponseEntity.ok(count);
    }

    /**
     * 커플 채팅방 생성
     */
    @PostMapping("/rooms")
    public ResponseEntity<CoupleChatRoom> createChatRoom(
            @RequestParam UUID coupleId,
            @RequestParam UUID user1Id,
            @RequestParam UUID user2Id) {
        
        CoupleChatRoom room = coupleChatService.getOrCreateChatRoom(coupleId, user1Id, user2Id);
        return ResponseEntity.ok(room);
    }
} 