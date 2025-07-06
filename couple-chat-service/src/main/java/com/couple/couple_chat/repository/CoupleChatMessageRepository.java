package com.couple.couple_chat.repository;

import com.couple.couple_chat.entity.CoupleChatMessage;
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
public interface CoupleChatMessageRepository extends JpaRepository<CoupleChatMessage, UUID> {

    @Query("SELECT c FROM CoupleChatMessage c WHERE c.roomId = :roomId ORDER BY c.createdAt DESC")
    Page<CoupleChatMessage> findByRoomIdOrderByCreatedAtDesc(@Param("roomId") UUID roomId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM CoupleChatMessage c WHERE c.roomId = :roomId AND c.senderId != :userId AND c.isRead = false")
    Long countUnreadMessages(@Param("roomId") UUID roomId, @Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE CoupleChatMessage c SET c.isRead = true, c.readAt = :readAt WHERE c.roomId = :roomId AND c.senderId != :userId AND c.isRead = false")
    void markMessagesAsRead(@Param("roomId") UUID roomId, @Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);

    @Query("SELECT c FROM CoupleChatMessage c WHERE c.roomId = :roomId AND c.createdAt > :since ORDER BY c.createdAt ASC")
    List<CoupleChatMessage> findNewMessagesSince(@Param("roomId") UUID roomId, @Param("since") LocalDateTime since);
} 