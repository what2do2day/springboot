package com.couple.mission_store.repository;

import com.couple.mission_store.entity.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ShopItemRepository extends JpaRepository<ShopItem, UUID> {
}