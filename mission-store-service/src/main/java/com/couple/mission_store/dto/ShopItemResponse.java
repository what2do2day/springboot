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
public class ShopItemResponse {

    private UUID id;
    private String name;
    private String description;
    private Integer priceCoin;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}