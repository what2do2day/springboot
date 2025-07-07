package com.couple.mission_store.controller;

import com.couple.mission_store.dto.ApiResponse;
import com.couple.mission_store.dto.CoupleItemResponse;
import com.couple.mission_store.dto.ShopItemRequest;
import com.couple.mission_store.dto.ShopItemResponse;
import com.couple.mission_store.service.CoupleItemService;
import com.couple.mission_store.service.ShopItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopItemService shopItemService;
    private final CoupleItemService coupleItemService;

    // 관리자용: 상점 아이템 등록
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<ShopItemResponse>> createShopItem(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId,
            @Valid @RequestBody ShopItemRequest request) {
        log.info("상점 아이템 등록 요청 - userId: {}, coupleId: {}, name: {}", userId, coupleId, request.getName());

        ShopItemResponse response = shopItemService.createShopItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("상점 아이템이 등록되었습니다.", response));
    }

    // 상점 아이템 목록 조회
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<ShopItemResponse>>> getAllShopItems(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId) {
        log.info("상점 아이템 목록 조회 요청 - userId: {}, coupleId: {}", userId, coupleId);

        List<ShopItemResponse> items = shopItemService.getAllShopItems();
        return ResponseEntity.ok(ApiResponse.success("상점 아이템 목록 조회 성공", items));
    }

    // 상점 아이템 상세 조회
    @GetMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<ShopItemResponse>> getShopItemById(
            @PathVariable UUID itemId,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader(value = "X-Couple-ID", required = false) String coupleId) {
        log.info("상점 아이템 상세 조회 - userId: {}, coupleId: {}, itemId: {}", userId, coupleId, itemId);

        ShopItemResponse response = shopItemService.getShopItemById(itemId);
        return ResponseEntity.ok(ApiResponse.success("상점 아이템 조회 성공", response));
    }

    // 커플 아이템 구매
    @PostMapping("/purchase/{itemId}")
    public ResponseEntity<ApiResponse<CoupleItemResponse>> purchaseItem(
            @PathVariable UUID itemId,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Couple-ID") String coupleId) {
        log.info("아이템 구매 요청 - userId: {}, coupleId: {}, itemId: {}", userId, coupleId, itemId);

        UUID coupleIdUUID = UUID.fromString(coupleId);
        CoupleItemResponse response = coupleItemService.purchaseItem(coupleIdUUID, itemId);
        return ResponseEntity.ok(ApiResponse.success("아이템 구매 성공", response));
    }

    // 커플이 보유한 아이템 목록 조회
    @GetMapping("/couple/items")
    public ResponseEntity<ApiResponse<List<CoupleItemResponse>>> getCoupleItems(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Couple-ID") String coupleId) {
        log.info("커플 아이템 목록 조회 - userId: {}, coupleId: {}", userId, coupleId);

        UUID coupleIdUUID = UUID.fromString(coupleId);
        List<CoupleItemResponse> items = coupleItemService.getCoupleItems(coupleIdUUID);
        return ResponseEntity.ok(ApiResponse.success("커플 아이템 목록 조회 성공", items));
    }

    // 커플이 보유한 활성화된 아이템 목록 조회
    @GetMapping("/couple/items/active")
    public ResponseEntity<ApiResponse<List<CoupleItemResponse>>> getActiveCoupleItems(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Couple-ID") String coupleId) {
        log.info("커플 활성화된 아이템 목록 조회 - userId: {}, coupleId: {}", userId, coupleId);

        UUID coupleIdUUID = UUID.fromString(coupleId);
        List<CoupleItemResponse> items = coupleItemService.getActiveCoupleItems(coupleIdUUID);
        return ResponseEntity.ok(ApiResponse.success("커플 활성화된 아이템 목록 조회 성공", items));
    }

    // 보유 아이템 활성화/비활성화
    @PutMapping("/couple/items/{coupleItemId}/toggle")
    public ResponseEntity<ApiResponse<CoupleItemResponse>> toggleItemActive(
            @PathVariable UUID coupleItemId,
            @RequestParam String active,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Couple-ID") String coupleId) {
        log.info("아이템 활성화 상태 변경 - userId: {}, coupleId: {}, coupleItemId: {}, active: {}",
                userId, coupleId, coupleItemId, active);

        UUID coupleIdUUID = UUID.fromString(coupleId);
        CoupleItemResponse response = coupleItemService.toggleItemActive(coupleIdUUID, coupleItemId, active);
        return ResponseEntity.ok(ApiResponse.success("아이템 활성화 상태 변경 성공", response));
    }
}