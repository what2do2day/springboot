package com.couple.user_couple.repository;

import com.couple.user_couple.entity.Couple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoupleRepository extends JpaRepository<Couple, UUID> {

    @Query("SELECT c FROM Couple c WHERE c.expired = 'N'")
    Optional<Couple> findActiveCoupleById(@Param("id") UUID id);

    @Query("SELECT c FROM Couple c WHERE c.expired = 'N' AND c.id = :id")
    Optional<Couple> findActiveCouple(@Param("id") UUID id);
}