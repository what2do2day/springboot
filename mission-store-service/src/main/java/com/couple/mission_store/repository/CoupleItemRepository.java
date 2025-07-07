package com.couple.mission_store.repository;

import com.couple.mission_store.entity.CoupleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CoupleItemRepository extends JpaRepository<CoupleItem, UUID> {

    List<CoupleItem> findByCoupleIdOrderByPurchasedAtDesc(UUID coupleId);

    List<CoupleItem> findByCoupleIdAndActiveOrderByPurchasedAtDesc(UUID coupleId, String active);

    @Modifying
    @Query("UPDATE CoupleItem ci SET ci.active = :active WHERE ci.id = :id AND ci.coupleId = :coupleId")
    int updateActiveStatus(@Param("id") UUID id, @Param("coupleId") UUID coupleId, @Param("active") String active);
}