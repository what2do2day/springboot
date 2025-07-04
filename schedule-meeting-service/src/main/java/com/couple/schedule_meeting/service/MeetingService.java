package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.MeetingResponse;
import com.couple.schedule_meeting.entity.Meeting;
import com.couple.schedule_meeting.entity.MeetingPlace;
import com.couple.schedule_meeting.entity.Route;
import com.couple.schedule_meeting.exception.MeetingAccessDeniedException;
import com.couple.schedule_meeting.exception.MeetingNotFoundException;
import com.couple.schedule_meeting.repository.MeetingPlaceRepository;
import com.couple.schedule_meeting.repository.MeetingRepository;
import com.couple.schedule_meeting.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {
    
    private final MeetingRepository meetingRepository;
    private final MeetingPlaceRepository meetingPlaceRepository;
    private final RouteRepository routeRepository;
    
    @Transactional(readOnly = true)
    public MeetingResponse getMeetingById(UUID meetingId, UUID coupleId) {
        // Meeting 조회
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(meetingId));
        
        // couple_id로 조회 권한 확인
        if (!meeting.getCoupleId().equals(coupleId)) {
            throw new MeetingAccessDeniedException(meetingId, coupleId);
        }
        
        // MeetingPlace 리스트 조회
        List<MeetingPlace> meetingPlaces = meetingPlaceRepository.findByMeetingIdOrderBySequenceAsc(meetingId);
        
        // Route 조회 (MongoDB에서)
        Route route = routeRepository.findById(meetingId.toString()).orElse(null);
        
        return MeetingResponse.from(meeting, meetingPlaces, route);
    }
} 