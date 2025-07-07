package com.couple.mission_store.service;

import com.couple.mission_store.dto.MissionRequest;
import com.couple.mission_store.dto.MissionResponse;
import com.couple.mission_store.entity.Mission;
import com.couple.mission_store.repository.MissionRepository;
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
public class MissionService {

    private final MissionRepository missionRepository;

    public MissionResponse createMission(MissionRequest request) {
        log.info("미션 생성 요청: {}", request.getTitle());

        Mission mission = Mission.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .coinReward(request.getCoinReward())
                .build();

        Mission savedMission = missionRepository.save(mission);
        log.info("미션 생성 완료: id={}", savedMission.getId());

        return convertToResponse(savedMission);
    }

    public List<MissionResponse> getAllMissions() {
        log.info("전체 미션 조회 요청");

        List<Mission> missions = missionRepository.findAll();
        return missions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public MissionResponse getMissionById(UUID missionId) {
        log.info("미션 조회 요청: missionId={}", missionId);

        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("미션을 찾을 수 없습니다: " + missionId));

        return convertToResponse(mission);
    }

    private MissionResponse convertToResponse(Mission mission) {
        return MissionResponse.builder()
                .id(mission.getId())
                .title(mission.getTitle())
                .description(mission.getDescription())
                .coinReward(mission.getCoinReward())
                .createdAt(mission.getCreatedAt())
                .updatedAt(mission.getUpdatedAt())
                .build();
    }
}