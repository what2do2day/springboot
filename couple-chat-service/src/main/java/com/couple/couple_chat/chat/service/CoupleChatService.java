package com.couple.couple_chat.chat.service;

import com.couple.couple_chat.chat.dto.ChatMessageRequest;
import com.couple.couple_chat.chat.dto.ChatMessageResponse;
import com.couple.couple_chat.chat.dto.MessageClassificationResponse;
import com.couple.couple_chat.chat.dto.ClassificationResult;
import com.couple.couple_chat.chat.entity.ChatMessage;
import com.couple.couple_chat.chat.entity.ChatRoom;
import com.couple.couple_chat.chat.repository.CoupleChatMessageRepository;
import com.couple.couple_chat.chat.repository.CoupleChatRoomRepository;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CoupleChatService {

    private static final String CLASSIFICATION_API_URL = "http://49.50.131.82:8000/api/v1/text/classify";

    private final CoupleChatRoomRepository chatRoomRepository;
    private final CoupleChatMessageRepository chatMessageRepository;
    private final WebClient webClient;

    /**
     * 커플 채팅방 생성 또는 조회
     */
    public ChatRoom getOrCreateChatRoom(UUID coupleId, UUID user1Id, UUID user2Id) {
        return chatRoomRepository.findByCoupleId(coupleId)
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.builder()
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
        // 사용자 정보 조회하여 coupleId 가져오기
        UUID coupleId = getCoupleIdByUserId(senderId);
        log.info("조회된 coupleId: {}", coupleId);
        
        // coupleId로 채팅방 조회
        log.info("채팅방 조회 시도: coupleId={}", coupleId);
        var roomOptional = chatRoomRepository.findByCoupleId(coupleId);
        if (roomOptional.isEmpty()) {
            log.error("채팅방을 찾을 수 없습니다. coupleId={}", coupleId);
            // 전체 채팅방 목록 조회해서 디버깅
            var allRooms = chatRoomRepository.findAll();
            log.info("전체 채팅방 목록:");
            allRooms.forEach(room -> log.info("  - roomId: {}, coupleId: {}", room.getId(), room.getCoupleId()));
            throw new RuntimeException("채팅방을 찾을 수 없습니다");
        }
        ChatRoom room = roomOptional.get();
        log.info("채팅방 조회 성공: roomId={}, coupleId={}", room.getId(), room.getCoupleId());

        // 메시지 저장
        ChatMessage message = ChatMessage.builder()
                .roomId(room.getId())  // 실제 roomId 사용
                .senderId(senderId)
                .message(request.getMessage())
                .messageType(request.getMessageType())
                .isRead(false)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // REST API 응답만 반환
        return convertToResponse(savedMessage);
    }

    /**
     * 채팅방 메시지 조회 (최신순)
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatMessages(UUID roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messages = chatMessageRepository
                .findByRoomIdOrderByCreatedAtDesc(roomId, pageable);

        return messages.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 메시지 조회 (시간순)
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatMessagesByTime(UUID roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messages = chatMessageRepository
                .findByRoomIdOrderByCreatedAtAsc(roomId, pageable);

        return messages.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 커플 ID로 채팅방 메시지 조회 (최신순)
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatMessagesByCoupleId(UUID coupleId) {
        List<ChatMessage> messages = chatMessageRepository
                .findByCoupleIdOrderByCreatedAtDesc(coupleId);

        return messages.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 커플 ID로 채팅방 메시지 조회 (시간순)
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatMessagesByCoupleIdAndTime(UUID coupleId) {
        List<ChatMessage> messages = chatMessageRepository
                .findByCoupleIdOrderByCreatedAtAsc(coupleId);

        return messages.stream()
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
     * 커플 ID로 메시지 읽음 처리
     */
    public void markMessagesAsReadByCoupleId(UUID coupleId, UUID userId) {
        chatMessageRepository.markMessagesAsReadByCoupleId(coupleId, userId, LocalDateTime.now());
    }

    /**
     * 읽지 않은 메시지 수 조회
     */
    @Transactional(readOnly = true)
    public Long getUnreadMessageCount(UUID roomId, UUID userId) {
        return chatMessageRepository.countUnreadMessages(roomId, userId);
    }

    /**
     * 커플 ID로 읽지 않은 메시지 수 조회
     */
    @Transactional(readOnly = true)
    public Long getUnreadMessageCountByCoupleId(UUID coupleId, UUID userId) {
        return chatMessageRepository.countUnreadMessagesByCoupleId(coupleId, userId);
    }

    /**
     * 사용자의 채팅방 조회
     */
    @Transactional(readOnly = true)
    public ChatRoom getUserChatRoom(UUID userId) {
        return chatRoomRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다"));
    }

    /**
     * 메시지 분류
     */
    public MessageClassificationResponse classifyMessage(UUID messageId) {
        // 메시지 조회
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("메시지를 찾을 수 없습니다"));

        // 외부 API 호출
        ClassificationResult classification = callExternalClassificationAPI(message.getMessage());

        return MessageClassificationResponse.builder()
                .messageId(messageId)
                .classification(classification)
                .build();
    }

    /**
     * 외부 분류 API 호출
     */
    private ClassificationResult callExternalClassificationAPI(String message) {
        try {
            // JSON 형태로 요청 body 구성
            Map<String, String> requestBody = Map.of("text", message);
            
            ClassificationResult response = webClient.post()
                    .uri(CLASSIFICATION_API_URL)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(ClassificationResult.class)
                    .block();

            return response != null ? response : ClassificationResult.builder()
                    .prediction("분류 실패")
                    .confidence(0.0)
                    .build();
        } catch (Exception e) {
            log.error("외부 분류 API 호출 실패: {}", e.getMessage());
            return ClassificationResult.builder()
                    .prediction("분류 실패")
                    .confidence(0.0)
                    .build();
        }
    }

    private ChatMessageResponse convertToResponse(ChatMessage message) {
        // 채팅방 정보 조회하여 coupleId 가져오기
        ChatRoom room = chatRoomRepository.findById(message.getRoomId())
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다"));
        
        // 사용자 이름 조회 (user-couple-service 호출)
        String senderName = getUserName(message.getSenderId());
        
        return ChatMessageResponse.builder()
                .id(message.getId())
                .coupleId(room.getCoupleId())
                .senderId(message.getSenderId())
                .senderName(senderName)
                .message(message.getMessage())
                .messageType(message.getMessageType())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }

    /**
     * 사용자 이름 조회
     */
    private String getUserName(UUID userId) {
        try {
            // user-couple-service의 사용자 정보 API 호출 (X-User-ID 헤더 사용)
            String response = webClient.get()
                    .uri("http://user-couple-service:8081/api/users/me")
                    .header("X-User-ID", userId.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            // JSON에서 name 필드 추출
            if (response != null && response.contains("\"name\"")) {
                int nameIndex = response.indexOf("\"name\":\"");
                if (nameIndex != -1) {
                    int startIndex = nameIndex + 8;
                    int endIndex = response.indexOf("\"", startIndex);
                    if (endIndex != -1) {
                        return response.substring(startIndex, endIndex);
                    }
                }
            }
            
            return "사용자";
        } catch (Exception e) {
            log.error("사용자 이름 조회 실패: userId={}, error={}", userId, e.getMessage());
            return "사용자";
        }
    }



    /**
     * 사용자 ID로 커플 ID 조회
     */
    private UUID getCoupleIdByUserId(UUID userId) {
        try {
            // user-couple-service의 사용자 정보 API 호출 (X-User-ID 헤더 사용)
            String response = webClient.get()
                    .uri("http://user-couple-service:8081/api/users/me")
                    .header("X-User-ID", userId.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            // JSON에서 coupleId 필드 추출
            if (response != null && response.contains("\"coupleId\"")) {
                int coupleIdIndex = response.indexOf("\"coupleId\":\"");
                if (coupleIdIndex != -1) {
                    int startIndex = coupleIdIndex + 12;
                    int endIndex = response.indexOf("\"", startIndex);
                    if (endIndex != -1) {
                        String coupleIdStr = response.substring(startIndex, endIndex);
                        return UUID.fromString(coupleIdStr);
                    }
                }
            }
            
            throw new RuntimeException("커플 ID를 찾을 수 없습니다");
        } catch (Exception e) {
            log.error("커플 ID 조회 실패: userId={}, error={}", userId, e.getMessage());
            throw new RuntimeException("커플 ID 조회 실패: " + e.getMessage());
        }
    }
} 