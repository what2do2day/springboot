package com.couple.schedule_meeting.repository;

import com.couple.schedule_meeting.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByUserIdOrderByStartTimeAsc(Long userId);

    List<Schedule> findByUserIdAndStartTimeBetweenOrderByStartTimeAsc(Long userId, LocalDateTime start,
            LocalDateTime end);

    List<Schedule> findByUserIdAndScheduleTypeOrderByStartTimeAsc(Long userId, Schedule.ScheduleType scheduleType);
}