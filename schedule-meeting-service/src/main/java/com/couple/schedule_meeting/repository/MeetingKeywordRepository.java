package com.couple.schedule_meeting.repository;

import com.couple.schedule_meeting.entity.MeetingKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MeetingKeywordRepository extends JpaRepository<MeetingKeyword, UUID> {
    
    @Query("SELECT mk FROM MeetingKeyword mk WHERE mk.meetingId = :meetingId ORDER BY mk.sequence ASC")
    List<MeetingKeyword> findByMeetingIdOrderBySequenceAsc(@Param("meetingId") UUID meetingId);
    
    @Query("SELECT mk FROM MeetingKeyword mk WHERE mk.keyword = :keyword")
    List<MeetingKeyword> findByKeyword(@Param("keyword") String keyword);
    
    void deleteByMeetingId(UUID meetingId);
} 