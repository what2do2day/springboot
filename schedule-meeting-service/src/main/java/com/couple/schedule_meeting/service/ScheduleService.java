package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.ScheduleCreateRequest;
import com.couple.schedule_meeting.dto.ScheduleResponse;
import com.couple.schedule_meeting.dto.ScheduleUpdateRequest;
import com.couple.schedule_meeting.entity.Schedule;
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

    public ScheduleResponse createSchedule(ScheduleCreateRequest request, UUID coupleId, UUID userId) {
        Schedule schedule = Schedule.builder()
                .coupleId(coupleId)
                .userId(userId)
                .name(request.getName())
                .message(request.getMessage())
                .dateTime(request.getDateTime())
                .build();
        
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return ScheduleResponse.from(savedSchedule);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getCoupleSchedule(UUID coupleId, Integer year, Integer month) {
        return scheduleRepository.findByCoupleIdAndYearAndMonthOrderByDayAsc(coupleId, year, month);
    }

    @Transactional(readOnly = true)
    public ScheduleResponse getScheduleById(UUID scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public ScheduleResponse updateSchedule(UUID scheduleId, ScheduleUpdateRequest request) {
        Schedule existingSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
        
        Schedule updatedSchedule = Schedule.builder()
                .id(existingSchedule.getId())
                .coupleId(existingSchedule.getCoupleId())
                .userId(existingSchedule.getUserId())
                .name(request.getName() != null ? request.getName() : existingSchedule.getName())
                .message(request.getMessage() != null ? request.getMessage() : existingSchedule.getMessage())
                .dateTime(request.getDateTime() != null ? request.getDateTime() : existingSchedule.getDateTime())
                .build();
        
        Schedule savedSchedule = scheduleRepository.save(updatedSchedule);
        return ScheduleResponse.from(savedSchedule);
    }

    @Transactional
    public void deleteSchedule(UUID scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
        scheduleRepository.delete(schedule);
    }
} 