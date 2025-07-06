package com.couple.schedule_meeting.tmp.service;

import com.couple.schedule_meeting.tmp.dto.TmpTestRequest;
import com.couple.schedule_meeting.tmp.dto.TmpTestResponse;
import com.couple.schedule_meeting.tmp.entity.TmpTestEntity;
import com.couple.schedule_meeting.tmp.repository.TmpTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmpTestService {

    private final TmpTestRepository tmpTestRepository;

    /**
     * 테스트 데이터를 MongoDB에 저장
     */
    public TmpTestResponse saveTestData(TmpTestRequest request) {
        LocalDateTime now = LocalDateTime.now();
        
        TmpTestEntity entity = TmpTestEntity.builder()
                .text(request.getText())
                .description(request.getDescription())
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        TmpTestEntity savedEntity = tmpTestRepository.save(entity);
        
        return convertToResponse(savedEntity);
    }

    /**
     * 모든 테스트 데이터 조회
     */
    public List<TmpTestResponse> getAllTestData() {
        List<TmpTestEntity> entities = tmpTestRepository.findAll();
        return entities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * ID로 특정 테스트 데이터 조회
     */
    public TmpTestResponse getTestDataById(String id) {
        return tmpTestRepository.findById(id)
                .map(this::convertToResponse)
                .orElse(null);
    }

    /**
     * 테스트 데이터 삭제
     */
    public boolean deleteTestData(String id) {
        if (tmpTestRepository.existsById(id)) {
            tmpTestRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private TmpTestResponse convertToResponse(TmpTestEntity entity) {
        return TmpTestResponse.builder()
                .id(entity.getId())
                .text(entity.getText())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
} 