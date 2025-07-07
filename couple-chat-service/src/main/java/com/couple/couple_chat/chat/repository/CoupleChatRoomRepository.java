package com.couple.couple_chat.chat.repository;

import com.couple.couple_chat.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoupleChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    @Query("SELECT c FROM ChatRoom c WHERE c.coupleId = :coupleId")
    Optional<ChatRoom> findByCoupleId(@Param("coupleId") UUID coupleId);

    @Query("SELECT c FROM ChatRoom c WHERE (c.user1Id = :userId OR c.user2Id = :userId) AND c.isActive = true")
    Optional<ChatRoom> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT c FROM ChatRoom c WHERE c.user1Id = :user1Id AND c.user2Id = :user2Id AND c.isActive = true")
    Optional<ChatRoom> findByUser1IdAndUser2Id(@Param("user1Id") UUID user1Id, @Param("user2Id") UUID user2Id);
} 