package com.couple.couple_chat.service;

import com.couple.couple_chat.dto.ChatMessageRequest;
import com.couple.couple_chat.dto.ChatMessageResponse;
import com.couple.couple_chat.entity.CoupleChatMessage;
import com.couple.couple_chat.entity.CoupleChatRoom;
import com.couple.couple_chat.repository.CoupleChatMessageRepository;
import com.couple.couple_chat.repository.CoupleChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CoupleChatService {

    private final CoupleChatRoomRepository chatRoomRepository;
    private final CoupleChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 커플 채팅방 생성 또는 조회
     */
    public CoupleChatRoom getOrCreateChatRoom(UUID coupleId, UUID user1Id, UUID user2Id) {
        return chatRoomRepository.findByCoupleId(coupleId)
                .orElseGet(() -> {
                    CoupleChatRoom newRoom = CoupleChatRoom.builder()
                            .coupleId(coupleId)
                            .user1Id(user1Id)
                            .user2Id(user2Id)
                            .roomName("커플 채팅방")
                            .isActive(true)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });
    }

    /**
     * 메시지 전송
     */
    public ChatMessageResponse sendMessage(UUID senderId, ChatMessageRequest request) {
        // 채팅방 존재 확인
        CoupleChatRoom room = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다"));

        // 메시지 저장
        CoupleChatMessage message = CoupleChatMessage.builder()
                .roomId(request.getRoomId())
                .senderId(senderId)
                .message(request.getMessage())
                .messageType(request.getMessageType())
                .isRead(false)
                .build();

        CoupleChatMessage savedMessage = chatMessageRepository.save(message);

        // WebSocket으로 실시간 전송
        ChatMessageResponse response = convertToResponse(savedMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + request.getRoomId(), response);

        return response;
    }

    /**
     * 채팅방 메시지 조회
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatMessages(UUID roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CoupleChatMessage> messages = chatMessageRepository
                .findByRoomIdOrderByCreatedAtDesc(roomId, pageable);

        return messages.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 메시지 읽음 처리
     */
    public void markMessagesAsRead(UUID roomId, UUID userId) {
        chatMessageRepository.markMessagesAsRead(roomId, userId, LocalDateTime.now());
    }

    /**
     * 읽지 않은 메시지 수 조회
     */
    @Transactional(readOnly = true)
    public Long getUnreadMessageCount(UUID roomId, UUID userId) {
        return chatMessageRepository.countUnreadMessages(roomId, userId);
    }

    /**
     * 사용자의 채팅방 조회
     */
    @Transactional(readOnly = true)
    public CoupleChatRoom getUserChatRoom(UUID userId) {
        return chatRoomRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다"));
    }

    private ChatMessageResponse convertToResponse(CoupleChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .roomId(message.getRoomId())
                .senderId(message.getSenderId())
                .senderName("사용자") // 실제로는 사용자 정보를 조회해야 함
                .message(message.getMessage())
                .messageType(message.getMessageType())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
} 