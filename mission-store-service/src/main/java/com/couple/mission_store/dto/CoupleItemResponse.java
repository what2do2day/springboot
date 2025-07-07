package com.couple.mission_store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoupleItemResponse {

    private UUID id;
    private UUID coupleId;
    private ShopItemResponse item;
    private LocalDateTime purchasedAt;
    private String active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}