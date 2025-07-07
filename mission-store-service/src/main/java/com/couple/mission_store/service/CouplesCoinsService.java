package com.couple.mission_store.service;

import com.couple.mission_store.dto.CouplesCoinsResponse;
import com.couple.mission_store.entity.CouplesCoins;
import com.couple.mission_store.repository.CouplesCoinsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CouplesCoinsService {

    private final CouplesCoinsRepository couplesCoinsRepository;

    public CouplesCoinsResponse getCoupleCoins(UUID coupleId) {
        log.info("커플 코인 조회 요청: coupleId={}", coupleId);

        CouplesCoins couplesCoins = couplesCoinsRepository.findByCoupleId(coupleId)
                .orElseGet(() -> createInitialCouplesCoins(coupleId));

        return convertToResponse(couplesCoins);
    }

    public void addCoins(UUID coupleId, Integer amount) {
        log.info("코인 추가 요청: coupleId={}, amount={}", coupleId, amount);

        CouplesCoins couplesCoins = couplesCoinsRepository.findByCoupleId(coupleId)
                .orElseGet(() -> createInitialCouplesCoins(coupleId));

        couplesCoinsRepository.addCoins(coupleId, amount);
        log.info("코인 추가 완료: coupleId={}, amount={}", coupleId, amount);
    }

    public boolean deductCoins(UUID coupleId, Integer amount) {
        log.info("코인 차감 요청: coupleId={}, amount={}", coupleId, amount);

        int updatedRows = couplesCoinsRepository.deductCoins(coupleId, amount);

        if (updatedRows > 0) {
            log.info("코인 차감 완료: coupleId={}, amount={}", coupleId, amount);
            return true;
        } else {
            log.warn("코인 부족으로 차감 실패: coupleId={}, amount={}", coupleId, amount);
            return false;
        }
    }

    private CouplesCoins createInitialCouplesCoins(UUID coupleId) {
        log.info("초기 커플 코인 생성: coupleId={}", coupleId);

        CouplesCoins couplesCoins = CouplesCoins.builder()
                .coupleId(coupleId)
                .totalCoin(0)
                .build();

        return couplesCoinsRepository.save(couplesCoins);
    }

    private CouplesCoinsResponse convertToResponse(CouplesCoins couplesCoins) {
        return CouplesCoinsResponse.builder()
                .id(couplesCoins.getId())
                .coupleId(couplesCoins.getCoupleId())
                .totalCoin(couplesCoins.getTotalCoin())
                .createdAt(couplesCoins.getCreatedAt())
                .updatedAt(couplesCoins.getUpdatedAt())
                .build();
    }
}