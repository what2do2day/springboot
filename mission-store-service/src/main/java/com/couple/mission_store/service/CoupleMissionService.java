package com.couple.mission_store.service;

import com.couple.mission_store.dto.CoupleMissionResponse;
import com.couple.mission_store.entity.CoupleMission;
import com.couple.mission_store.entity.Mission;
import com.couple.mission_store.repository.CoupleMissionRepository;
import com.couple.mission_store.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CoupleMissionService {

    private final CoupleMissionRepository coupleMissionRepository;
    private final MissionRepository missionRepository;
    private final CouplesCoinsService couplesCoinsService;

    public List<CoupleMissionResponse> getCoupleMissions(UUID coupleId) {
        log.info("커플 미션 목록 조회 요청: coupleId={}", coupleId);

        List<CoupleMission> coupleMissions = coupleMissionRepository.findByCoupleIdOrderByAssignedDateDesc(coupleId);
        return coupleMissions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public CoupleMissionResponse completeMission(UUID coupleId, LocalDate assignedDate) {
        log.info("미션 완료 요청: coupleId={}, assignedDate={}", coupleId, assignedDate);

        CoupleMission coupleMission = coupleMissionRepository
                .findIncompleteMissionByCoupleIdAndDate(coupleId, assignedDate)
                .orElseThrow(() -> new IllegalArgumentException("완료할 미션을 찾을 수 없습니다: " + assignedDate));

        if ("Y".equals(coupleMission.getCompleted())) {
            throw new IllegalArgumentException("이미 완료된 미션입니다: " + assignedDate);
        }

        // 미션 완료 처리
        coupleMission.setCompleted("Y");
        coupleMission.setCompletedAt(java.time.LocalDateTime.now());
        CoupleMission savedCoupleMission = coupleMissionRepository.save(coupleMission);

        // 코인 보상 지급
        Mission mission = coupleMission.getMission();
        couplesCoinsService.addCoins(coupleId, mission.getCoinReward());

        log.info("미션 완료 처리 완료: coupleId={}, missionId={}, coinReward={}",
                coupleId, mission.getId(), mission.getCoinReward());

        return convertToResponse(savedCoupleMission);
    }

    public CoupleMissionResponse getTodayMission(UUID coupleId) {
        log.info("오늘 미션 조회 요청: coupleId={}", coupleId);

        LocalDate today = LocalDate.now();
        CoupleMission coupleMission = coupleMissionRepository.findByCoupleIdAndAssignedDate(coupleId, today)
                .orElseThrow(() -> new IllegalArgumentException("오늘의 미션을 찾을 수 없습니다"));

        return convertToResponse(coupleMission);
    }

    private CoupleMissionResponse convertToResponse(CoupleMission coupleMission) {
        return CoupleMissionResponse.builder()
                .id(coupleMission.getId())
                .coupleId(coupleMission.getCoupleId())
                .mission(convertMissionToResponse(coupleMission.getMission()))
                .assignedDate(coupleMission.getAssignedDate())
                .completed(coupleMission.getCompleted())
                .completedAt(coupleMission.getCompletedAt())
                .createdAt(coupleMission.getCreatedAt())
                .updatedAt(coupleMission.getUpdatedAt())
                .build();
    }

    private com.couple.mission_store.dto.MissionResponse convertMissionToResponse(Mission mission) {
        return com.couple.mission_store.dto.MissionResponse.builder()
                .id(mission.getId())
                .title(mission.getTitle())
                .description(mission.getDescription())
                .coinReward(mission.getCoinReward())
                .createdAt(mission.getCreatedAt())
                .updatedAt(mission.getUpdatedAt())
                .build();
    }
}