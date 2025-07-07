package com.couple.couple_chat.location.repository;

import com.couple.couple_chat.location.entity.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocation, UUID> {

    @Query("SELECT u FROM UserLocation u WHERE u.userId = :userId AND u.isActive = true")
    Optional<UserLocation> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT u FROM UserLocation u WHERE u.coupleId = :coupleId AND u.isActive = true")
    List<UserLocation> findByCoupleId(@Param("coupleId") UUID coupleId);

    @Query("SELECT u FROM UserLocation u WHERE u.coupleId = :coupleId AND u.userId != :userId AND u.isActive = true")
    Optional<UserLocation> findByCoupleIdAndUserIdNot(@Param("coupleId") UUID coupleId, @Param("userId") UUID userId);

    @Query("SELECT u FROM UserLocation u WHERE u.coupleId = :coupleId AND u.isActive = true ORDER BY u.updatedAt DESC")
    List<UserLocation> findLatestByCoupleId(@Param("coupleId") UUID coupleId);
} 