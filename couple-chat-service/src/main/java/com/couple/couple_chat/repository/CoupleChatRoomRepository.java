package com.couple.couple_chat.repository;

import com.couple.couple_chat.entity.CoupleChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoupleChatRoomRepository extends JpaRepository<CoupleChatRoom, UUID> {

    @Query("SELECT c FROM CoupleChatRoom c WHERE c.coupleId = :coupleId AND c.isActive = true")
    Optional<CoupleChatRoom> findByCoupleId(@Param("coupleId") UUID coupleId);

    @Query("SELECT c FROM CoupleChatRoom c WHERE (c.user1Id = :userId OR c.user2Id = :userId) AND c.isActive = true")
    Optional<CoupleChatRoom> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT c FROM CoupleChatRoom c WHERE c.user1Id = :user1Id AND c.user2Id = :user2Id AND c.isActive = true")
    Optional<CoupleChatRoom> findByUser1IdAndUser2Id(@Param("user1Id") UUID user1Id, @Param("user2Id") UUID user2Id);
} 