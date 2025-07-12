package com.couple.couple_chat.chat.controller;

import com.couple.couple_chat.chat.dto.ChatMessageRequest;
import com.couple.couple_chat.chat.dto.ChatMessageResponse;
import com.couple.couple_chat.chat.dto.CreateChatRoomRequest;
import com.couple.couple_chat.chat.dto.MessageClassificationResponse;
import com.couple.couple_chat.chat.entity.ChatRoom;
import com.couple.couple_chat.chat.service.CoupleChatService;
import com.couple.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/couple-chat")
@RequiredArgsConstructor
public class CoupleChatController {

    private final CoupleChatService coupleChatService;

    /**
     * REST API를 통한 메시지 전송
     */
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @RequestHeader("X-User-ID") UUID senderId,
            @Valid @RequestBody ChatMessageRequest request) {
        
        ChatMessageResponse response = coupleChatService.sendMessage(senderId, request);
        return ResponseEntity.ok(ApiResponse.success("메시지가 전송되었습니다.", response));
    }

    /**
     * 채팅방 메시지 조회 (최신순)
     */
    @GetMapping("/couples/{coupleId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatMessages(
            @PathVariable UUID coupleId) {
        
        List<ChatMessageResponse> messages = coupleChatService.getChatMessagesByCoupleId(coupleId);
        return ResponseEntity.ok(ApiResponse.success("메시지 목록을 조회했습니다.", messages));
    }

    /**
     * 채팅방 메시지 조회 (시간순)
     */
    @GetMapping("/couples/{coupleId}/messages/time")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatMessagesByTime(
            @PathVariable UUID coupleId) {
        
        List<ChatMessageResponse> messages = coupleChatService.getChatMessagesByCoupleIdAndTime(coupleId);
        return ResponseEntity.ok(ApiResponse.success("메시지 목록을 조회했습니다.", messages));
    }

    /**
     * 사용자의 채팅방 조회
     */
    @GetMapping("/rooms/my")
    public ResponseEntity<ApiResponse<ChatRoom>> getMyChatRoom(@RequestHeader("X-User-ID") UUID userId) {
        ChatRoom room = coupleChatService.getUserChatRoom(userId);
        return ResponseEntity.ok(ApiResponse.success("채팅방을 조회했습니다.", room));
    }

    /**
     * 메시지 읽음 처리
     */
    @PutMapping("/couples/{coupleId}/read")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(
            @PathVariable UUID coupleId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        coupleChatService.markMessagesAsReadByCoupleId(coupleId, userId);
        return ResponseEntity.ok(ApiResponse.success("메시지를 읽음 처리했습니다.", null));
    }

    /**
     * 읽지 않은 메시지 수 조회
     */
    @GetMapping("/couples/{coupleId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadMessageCount(
            @PathVariable UUID coupleId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        Long count = coupleChatService.getUnreadMessageCountByCoupleId(coupleId, userId);
        return ResponseEntity.ok(ApiResponse.success("읽지 않은 메시지 수를 조회했습니다.", count));
    }

    /**
     * 커플 채팅방 생성
     */
    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<ChatRoom>> createChatRoom(
            @RequestParam UUID coupleId,
            @RequestParam UUID user1Id,
            @RequestParam UUID user2Id) {
        
        ChatRoom room = coupleChatService.getOrCreateChatRoom(coupleId, user1Id, user2Id);
        return ResponseEntity.ok(ApiResponse.success("채팅방이 생성되었습니다.", room));
    }

    /**
     * 커플 채팅방 생성 (요청 본문 방식)
     */
    @PostMapping("/rooms/body")
    public ResponseEntity<ApiResponse<ChatRoom>> createChatRoomWithBody(
            @RequestBody CreateChatRoomRequest request) {
        
        ChatRoom room = coupleChatService.getOrCreateChatRoom(
            request.getCoupleId(), 
            request.getUser1Id(), 
            request.getUser2Id()
        );
        return ResponseEntity.ok(ApiResponse.success("채팅방이 생성되었습니다.", room));
    }

    /**
     * 메시지 감정분석 요청
     */
    @GetMapping("/messages/{messageId}/classify")
    public ResponseEntity<ApiResponse<MessageClassificationResponse>> classifyMessage(
            @PathVariable UUID messageId) {
        
        MessageClassificationResponse response = coupleChatService.classifyMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success("메시지 감정분석이 완료되었습니다.", response));
    }
} 