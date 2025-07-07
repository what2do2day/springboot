package com.couple.mission_store.service;

import com.couple.mission_store.dto.CoupleItemResponse;
import com.couple.mission_store.entity.CoupleItem;
import com.couple.mission_store.entity.ShopItem;
import com.couple.mission_store.repository.CoupleItemRepository;
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
public class CoupleItemService {

    private final CoupleItemRepository coupleItemRepository;
    private final ShopItemRepository shopItemRepository;
    private final CouplesCoinsService couplesCoinsService;

    public List<CoupleItemResponse> getCoupleItems(UUID coupleId) {
        log.info("커플 아이템 목록 조회 요청: coupleId={}", coupleId);

        List<CoupleItem> coupleItems = coupleItemRepository.findByCoupleIdOrderByPurchasedAtDesc(coupleId);
        return coupleItems.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<CoupleItemResponse> getActiveCoupleItems(UUID coupleId) {
        log.info("커플 활성화된 아이템 목록 조회 요청: coupleId={}", coupleId);

        List<CoupleItem> coupleItems = coupleItemRepository.findByCoupleIdAndActiveOrderByPurchasedAtDesc(coupleId,
                "Y");
        return coupleItems.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public CoupleItemResponse purchaseItem(UUID coupleId, UUID itemId) {
        log.info("아이템 구매 요청: coupleId={}, itemId={}", coupleId, itemId);

        ShopItem shopItem = shopItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상점 아이템을 찾을 수 없습니다: " + itemId));

        // 코인 차감
        boolean deductionSuccess = couplesCoinsService.deductCoins(coupleId, shopItem.getPriceCoin());
        if (!deductionSuccess) {
            throw new IllegalArgumentException("코인이 부족합니다. 필요 코인: " + shopItem.getPriceCoin());
        }

        // 아이템 구매
        CoupleItem coupleItem = CoupleItem.builder()
                .coupleId(coupleId)
                .item(shopItem)
                .active("N") // 기본적으로 비활성화 상태
                .build();

        CoupleItem savedCoupleItem = coupleItemRepository.save(coupleItem);
        log.info("아이템 구매 완료: coupleId={}, itemId={}, price={}", coupleId, itemId, shopItem.getPriceCoin());

        return convertToResponse(savedCoupleItem);
    }

    public CoupleItemResponse toggleItemActive(UUID coupleId, UUID coupleItemId, String active) {
        log.info("아이템 활성화 상태 변경 요청: coupleId={}, coupleItemId={}, active={}", coupleId, coupleItemId, active);

        if (!"Y".equals(active) && !"N".equals(active)) {
            throw new IllegalArgumentException("활성화 상태는 'Y' 또는 'N'이어야 합니다.");
        }

        int updatedRows = coupleItemRepository.updateActiveStatus(coupleItemId, coupleId, active);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("아이템을 찾을 수 없거나 권한이 없습니다: " + coupleItemId);
        }

        CoupleItem coupleItem = coupleItemRepository.findById(coupleItemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템을 찾을 수 없습니다: " + coupleItemId));

        log.info("아이템 활성화 상태 변경 완료: coupleId={}, coupleItemId={}, active={}", coupleId, coupleItemId, active);

        return convertToResponse(coupleItem);
    }

    private CoupleItemResponse convertToResponse(CoupleItem coupleItem) {
        return CoupleItemResponse.builder()
                .id(coupleItem.getId())
                .coupleId(coupleItem.getCoupleId())
                .item(convertShopItemToResponse(coupleItem.getItem()))
                .purchasedAt(coupleItem.getPurchasedAt())
                .active(coupleItem.getActive())
                .createdAt(coupleItem.getCreatedAt())
                .updatedAt(coupleItem.getUpdatedAt())
                .build();
    }

    private com.couple.mission_store.dto.ShopItemResponse convertShopItemToResponse(ShopItem shopItem) {
        return com.couple.mission_store.dto.ShopItemResponse.builder()
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