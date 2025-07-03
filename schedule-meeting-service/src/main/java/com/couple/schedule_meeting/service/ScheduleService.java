package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.ScheduleCreateRequest;
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
    public Schedule getScheduleById(UUID scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
    }

    @Transactional
    public Schedule updateSchedule(UUID scheduleId, ScheduleUpdateRequest request) {
        Schedule existingSchedule = getScheduleById(scheduleId);
        
        Schedule updatedSchedule = Schedule.builder()
                .id(existingSchedule.getId())
                .coupleId(existingSchedule.getCoupleId())
                .userId(existingSchedule.getUserId())
                .name(request.getName() != null ? request.getName() : existingSchedule.getName())
                .message(request.getMessage() != null ? request.getMessage() : existingSchedule.getMessage())
                .dateTime(request.getDateTime() != null ? request.getDateTime() : existingSchedule.getDateTime())
                .build();
        
        return scheduleRepository.save(updatedSchedule);
    }

    @Transactional
    public void deleteSchedule(UUID scheduleId) {
        Schedule schedule = getScheduleById(scheduleId);
        scheduleRepository.delete(schedule);
    }
} 