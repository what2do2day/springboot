package com.couple.mission_store.service;

import com.couple.mission_store.dto.ShopItemRequest;
import com.couple.mission_store.dto.ShopItemResponse;
import com.couple.mission_store.entity.ShopItem;
import com.couple.mission_store.repository.ShopItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShopItemService {

    private final ShopItemRepository shopItemRepository;

    public ShopItemResponse createShopItem(ShopItemRequest request) {
        log.info("상점 아이템 생성 요청: {}", request.getName());

        ShopItem shopItem = ShopItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .priceCoin(request.getPriceCoin())
                .imageUrl(request.getImageUrl())
                .build();

        ShopItem savedShopItem = shopItemRepository.save(shopItem);
        log.info("상점 아이템 생성 완료: id={}", savedShopItem.getId());

        return convertToResponse(savedShopItem);
    }

    public List<ShopItemResponse> getAllShopItems() {
        log.info("전체 상점 아이템 조회 요청");

        List<ShopItem> shopItems = shopItemRepository.findAll();
        return shopItems.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ShopItemResponse getShopItemById(UUID itemId) {
        log.info("상점 아이템 조회 요청: itemId={}", itemId);

        ShopItem shopItem = shopItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상점 아이템을 찾을 수 없습니다: " + itemId));

        return convertToResponse(shopItem);
    }

    private ShopItemResponse convertToResponse(ShopItem shopItem) {
        return ShopItemResponse.builder()
                .id(shopItem.getId())
                .name(shopItem.getName())
                .description(shopItem.getDescription())
                .priceCoin(shopItem.getPriceCoin())
                .imageUrl(shopItem.getImageUrl())
                .createdAt(shopItem.getCreatedAt())
                .updatedAt(shopItem.getUpdatedAt())
                .build();
    }
}