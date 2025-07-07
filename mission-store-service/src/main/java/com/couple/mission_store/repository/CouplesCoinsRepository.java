package com.couple.mission_store.repository;

import com.couple.mission_store.entity.CouplesCoins;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouplesCoinsRepository extends JpaRepository<CouplesCoins, UUID> {

    Optional<CouplesCoins> findByCoupleId(UUID coupleId);

    @Modifying
    @Query("UPDATE CouplesCoins cc SET cc.totalCoin = cc.totalCoin + :amount WHERE cc.coupleId = :coupleId")
    void addCoins(@Param("coupleId") UUID coupleId, @Param("amount") Integer amount);

    @Modifying
    @Query("UPDATE CouplesCoins cc SET cc.totalCoin = cc.totalCoin - :amount WHERE cc.coupleId = :coupleId AND cc.totalCoin >= :amount")
    int deductCoins(@Param("coupleId") UUID coupleId, @Param("amount") Integer amount);
}