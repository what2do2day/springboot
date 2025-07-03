package com.couple.user_couple.repository;

import com.couple.user_couple.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE u.coupleId = :coupleId")
    List<User> findByCoupleId(@Param("coupleId") UUID coupleId);

    @Query("SELECT u FROM User u WHERE u.coupleId = :coupleId AND u.id != :userId")
    Optional<User> findPartnerByCoupleIdAndUserId(@Param("coupleId") UUID coupleId, @Param("userId") UUID userId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.coupleId = :coupleId")
    long countByCoupleId(@Param("coupleId") UUID coupleId);

    Optional<User> findByName(String name);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}