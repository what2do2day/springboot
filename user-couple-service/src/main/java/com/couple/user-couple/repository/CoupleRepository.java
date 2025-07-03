package com.couple.user_couple.repository;

import com.couple.user_couple.entity.Couple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoupleRepository extends JpaRepository<Couple, Long> {

    @Query("SELECT c FROM Couple c WHERE (c.user1.id = :userId OR c.user2.id = :userId) AND c.status = 'ACTIVE'")
    Optional<Couple> findByUserIdAndActive(@Param("userId") Long userId);

    @Query("SELECT c FROM Couple c WHERE c.user1.id = :userId OR c.user2.id = :userId")
    Optional<Couple> findByUserId(@Param("userId") Long userId);

    boolean existsByUser1IdOrUser2Id(Long user1Id, Long user2Id);
}