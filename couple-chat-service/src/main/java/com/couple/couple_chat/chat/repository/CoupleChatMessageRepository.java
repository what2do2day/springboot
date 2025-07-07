package com.couple.couple_chat.chat.repository;

import com.couple.couple_chat.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CoupleChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("SELECT c FROM ChatMessage c WHERE c.roomId = :roomId ORDER BY c.createdAt DESC")
    Page<ChatMessage> findByRoomIdOrderByCreatedAtDesc(@Param("roomId") UUID roomId, Pageable pageable);

    @Query("SELECT c FROM ChatMessage c WHERE c.roomId = :roomId ORDER BY c.createdAt ASC")
    Page<ChatMessage> findByRoomIdOrderByCreatedAtAsc(@Param("roomId") UUID roomId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM ChatMessage c WHERE c.roomId = :roomId AND c.senderId != :userId AND c.isRead = false")
    Long countUnreadMessages(@Param("roomId") UUID roomId, @Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE ChatMessage c SET c.isRead = true, c.readAt = :readAt WHERE c.roomId = :roomId AND c.senderId != :userId AND c.isRead = false")
    void markMessagesAsRead(@Param("roomId") UUID roomId, @Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);

    @Query("SELECT c FROM ChatMessage c WHERE c.roomId = :roomId AND c.createdAt > :since ORDER BY c.createdAt ASC")
    List<ChatMessage> findNewMessagesSince(@Param("roomId") UUID roomId, @Param("since") LocalDateTime since);

    // coupleId로 메시지 조회 (JOIN 사용)
    @Query("SELECT c FROM ChatMessage c JOIN ChatRoom r ON c.roomId = r.id WHERE r.coupleId = :coupleId ORDER BY c.createdAt DESC")
    Page<ChatMessage> findByCoupleIdOrderByCreatedAtDesc(@Param("coupleId") UUID coupleId, Pageable pageable);

    @Query("SELECT c FROM ChatMessage c JOIN ChatRoom r ON c.roomId = r.id WHERE r.coupleId = :coupleId ORDER BY c.createdAt ASC")
    Page<ChatMessage> findByCoupleIdOrderByCreatedAtAsc(@Param("coupleId") UUID coupleId, Pageable pageable);

    // 페이징 없는 메서드들
    @Query("SELECT c FROM ChatMessage c JOIN ChatRoom r ON c.roomId = r.id WHERE r.coupleId = :coupleId ORDER BY c.createdAt DESC")
    List<ChatMessage> findByCoupleIdOrderByCreatedAtDesc(@Param("coupleId") UUID coupleId);

    @Query("SELECT c FROM ChatMessage c JOIN ChatRoom r ON c.roomId = r.id WHERE r.coupleId = :coupleId ORDER BY c.createdAt ASC")
    List<ChatMessage> findByCoupleIdOrderByCreatedAtAsc(@Param("coupleId") UUID coupleId);

    @Query("SELECT COUNT(c) FROM ChatMessage c JOIN ChatRoom r ON c.roomId = r.id WHERE r.coupleId = :coupleId AND c.senderId != :userId AND c.isRead = false")
    Long countUnreadMessagesByCoupleId(@Param("coupleId") UUID coupleId, @Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE ChatMessage c SET c.isRead = true, c.readAt = :readAt WHERE c.roomId IN (SELECT r.id FROM ChatRoom r WHERE r.coupleId = :coupleId) AND c.senderId != :userId AND c.isRead = false")
    void markMessagesAsReadByCoupleId(@Param("coupleId") UUID coupleId, @Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);
} 