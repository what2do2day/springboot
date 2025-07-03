package com.couple.schedule_meeting.repository;

import com.couple.schedule_meeting.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
    @Query("SELECT s FROM Schedule s WHERE s.coupleId = :coupleId AND s.year = :year AND s.month = :month ORDER BY s.day ASC")
    List<Schedule> findByCoupleIdAndYearAndMonthOrderByDayAsc(@Param("coupleId") UUID coupleId, 
                                                             @Param("year") Integer year, 
                                                             @Param("month") Integer month);
} 