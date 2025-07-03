package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.ScheduleCreateRequest;
import com.couple.schedule_meeting.dto.ScheduleUpdateRequest;
import com.couple.schedule_meeting.entity.Schedule;
import com.couple.schedule_meeting.exception.ScheduleAccessDeniedException;
import com.couple.schedule_meeting.exception.ScheduleNotFoundException;
import com.couple.schedule_meeting.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;

    public Schedule createSchedule(ScheduleCreateRequest request, UUID coupleId, UUID userId) {
        Schedule schedule = Schedule.builder()
                .coupleId(coupleId)
                .userId(userId)
                .name(request.getName())
                .message(request.getMessage())
                .dateTime(request.getDateTime())
                .build();
        
        return scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getCoupleSchedule(UUID coupleId, Integer year, Integer month) {
        return scheduleRepository.findByCoupleIdAndYearAndMonthOrderByDayAsc(coupleId, year, month);
    }

    @Transactional(readOnly = true)
    public Schedule getScheduleById(UUID scheduleId, UUID coupleId) {
        return findScheduleWithPermission(scheduleId, coupleId);
    }

    @Transactional
    public Schedule updateSchedule(UUID scheduleId, ScheduleUpdateRequest request, UUID coupleId) {
        Schedule existingSchedule = findScheduleWithPermission(scheduleId, coupleId);
        
        Schedule updatedSchedule = Schedule.builder()
                .id(existingSchedule.getId())
                .coupleId(existingSchedule.getCoupleId())
                .userId(existingSchedule.getUserId())
                .name(getValueOrDefault(request.getName(), existingSchedule.getName()))
                .message(getValueOrDefault(request.getMessage(), existingSchedule.getMessage()))
                .dateTime(getValueOrDefault(request.getDateTime(), existingSchedule.getDateTime()))
                .build();
        
        return scheduleRepository.save(updatedSchedule);
    }

    @Transactional
    public void deleteSchedule(UUID scheduleId, UUID coupleId) {
        Schedule schedule = findScheduleWithPermission(scheduleId, coupleId);
        scheduleRepository.delete(schedule);
    }

    /**
     * 일정을 조회하고 권한을 확인하는 공통 메서드
     */
    private Schedule findScheduleWithPermission(UUID scheduleId, UUID coupleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
        
        // 권한 확인: 일정이 해당 커플에 속하는지 확인
        if (!schedule.getCoupleId().equals(coupleId)) {
            throw new ScheduleAccessDeniedException(scheduleId, coupleId);
        }
        
        return schedule;
    }

    /**
     * null이 아닌 경우 새로운 값을, null인 경우 기본값을 반환하는 유틸리티 메서드
     */
    private <T> T getValueOrDefault(T newValue, T defaultValue) {
        return newValue != null ? newValue : defaultValue;
    }
} 