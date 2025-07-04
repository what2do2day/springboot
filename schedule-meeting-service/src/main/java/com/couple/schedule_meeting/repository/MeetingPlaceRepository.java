package com.couple.schedule_meeting.repository;

import com.couple.schedule_meeting.entity.MeetingPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MeetingPlaceRepository extends JpaRepository<MeetingPlace, UUID> {
    
    @Query("SELECT mp FROM MeetingPlace mp WHERE mp.meetingId = :meetingId ORDER BY mp.sequence ASC")
    List<MeetingPlace> findByMeetingIdOrderBySequenceAsc(@Param("meetingId") UUID meetingId);
    
    @Query("SELECT mp FROM MeetingPlace mp WHERE mp.name = :name")
    List<MeetingPlace> findByName(@Param("name") String name);
    
    void deleteByMeetingId(UUID meetingId);
} 