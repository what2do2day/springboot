package com.couple.couple_chat.location.repository;

import com.couple.couple_chat.location.entity.LocationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, UUID> {

    @Query("SELECT l FROM LocationHistory l WHERE l.coupleId = :coupleId ORDER BY l.createdAt DESC")
    Page<LocationHistory> findByCoupleIdOrderByCreatedAtDesc(@Param("coupleId") UUID coupleId, Pageable pageable);

    @Query("SELECT l FROM LocationHistory l WHERE l.userId = :userId ORDER BY l.createdAt DESC")
    Page<LocationHistory> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT l FROM LocationHistory l WHERE l.coupleId = :coupleId AND l.createdAt >= :since ORDER BY l.createdAt ASC")
    List<LocationHistory> findRecentByCoupleId(@Param("coupleId") UUID coupleId, @Param("since") LocalDateTime since);

    @Query("SELECT l FROM LocationHistory l WHERE l.roomId = :roomId ORDER BY l.createdAt DESC")
    List<LocationHistory> findByRoomIdOrderByCreatedAtDesc(@Param("roomId") UUID roomId);
} 