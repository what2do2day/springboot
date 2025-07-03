package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.ScheduleRequest;
import com.couple.schedule_meeting.entity.Schedule;
import com.couple.schedule_meeting.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public Schedule createSchedule(Long userId, ScheduleRequest request) {
        Schedule.ScheduleType scheduleType = Schedule.ScheduleType.valueOf(
                request.getScheduleType().toUpperCase());

        Schedule schedule = Schedule.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .scheduleType(scheduleType)
                .build();

        return scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByUserId(Long userId) {
        return scheduleRepository.findByUserIdOrderByStartTimeAsc(userId);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return scheduleRepository.findByUserIdAndStartTimeBetweenOrderByStartTimeAsc(userId, start, end);
    }

    @Transactional(readOnly = true)
    public Schedule getScheduleById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));
    }

    public Schedule updateSchedule(Long scheduleId, ScheduleRequest request) {
        Schedule schedule = getScheduleById(scheduleId);

        schedule.setTitle(request.getTitle());
        schedule.setDescription(request.getDescription());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setLocation(request.getLocation());

        if (request.getScheduleType() != null) {
            schedule.setScheduleType(Schedule.ScheduleType.valueOf(request.getScheduleType().toUpperCase()));
        }

        return scheduleRepository.save(schedule);
    }

    public void deleteSchedule(Long scheduleId) {
        Schedule schedule = getScheduleById(scheduleId);
        scheduleRepository.delete(schedule);
    }
}