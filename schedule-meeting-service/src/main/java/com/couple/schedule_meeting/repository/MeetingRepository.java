package com.couple.schedule_meeting.repository;

import com.couple.schedule_meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, UUID> {
    
    @Query("SELECT m FROM Meeting m WHERE m.coupleId = :coupleId AND m.year = :year AND m.month = :month ORDER BY m.startTime ASC")
    List<Meeting> findByCoupleIdAndYearAndMonthOrderByStartTimeAsc(@Param("coupleId") UUID coupleId, 
                                                                  @Param("year") Integer year, 
                                                                  @Param("month") Integer month);
    
    @Query("SELECT m FROM Meeting m WHERE m.coupleId = :coupleId ORDER BY m.startTime ASC")
    List<Meeting> findByCoupleIdOrderByStartTimeAsc(@Param("coupleId") UUID coupleId);
} 