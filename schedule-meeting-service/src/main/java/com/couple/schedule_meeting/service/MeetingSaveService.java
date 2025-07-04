package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.WaypointRouteResponse;
import com.couple.schedule_meeting.entity.*;
import com.couple.schedule_meeting.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingSaveService {

    private final TmpMeetingRepository tmpMeetingRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingPlaceRepository meetingPlaceRepository;
    private final MeetingKeywordRepository meetingKeywordRepository;
    private final RouteRepository routeRepository;

    /**
     * TmpMeeting 문서를 조회하여 Meeting, MeetingPlaces, MeetingKeywords, Route를 저장
     */
    @Transactional
    public UUID saveMeetingFromTmpMeeting(String tmpMeetingId, UUID coupleId) {
        try {
            log.info("TmpMeeting에서 Meeting 저장 시작: tmpMeetingId={}, coupleId={}", tmpMeetingId, coupleId);
            
            // 1. TmpMeeting 문서 조회
            TmpMeeting tmpMeeting = tmpMeetingRepository.findById(tmpMeetingId)
                    .orElseThrow(() -> new RuntimeException("TmpMeeting을 찾을 수 없습니다: " + tmpMeetingId));
            
            log.info("TmpMeeting 조회 성공: name={}", tmpMeeting.getName());
            
            // 2. Meeting 엔티티 생성 및 저장
            Meeting meeting = createMeetingFromTmpMeeting(tmpMeeting, coupleId);
            Meeting savedMeeting = meetingRepository.save(meeting);
            
            log.info("Meeting 저장 성공: meetingId={}", savedMeeting.getId());
            
            // 3. Route 저장
            saveRouteFromTmpMeeting(tmpMeeting, savedMeeting.getId());
            
            // 4. MeetingPlaces 저장
            saveMeetingPlacesFromTmpMeeting(tmpMeeting, savedMeeting.getId());
            
            // 5. MeetingKeywords 저장
            saveMeetingKeywordsFromTmpMeeting(tmpMeeting, savedMeeting.getId());
            
            // 6. TmpMeeting 문서 삭제 (선택사항)
            tmpMeetingRepository.deleteById(tmpMeetingId);
            log.info("TmpMeeting 문서 삭제 완료: tmpMeetingId={}", tmpMeetingId);
            
            log.info("Meeting 저장 완료: meetingId={}", savedMeeting.getId());
            return savedMeeting.getId();
            
        } catch (Exception e) {
            log.error("Meeting 저장 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("Meeting 저장 실패", e);
        }
    }
    
    /**
     * TmpMeeting에서 Meeting 엔티티 생성
     */
    private Meeting createMeetingFromTmpMeeting(TmpMeeting tmpMeeting, UUID coupleId) {
        // date를 LocalDateTime으로 변환
        LocalDateTime startDateTime = LocalDateTime.of(tmpMeeting.getDate(), tmpMeeting.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.of(tmpMeeting.getDate(), tmpMeeting.getEndTime());
        
        return Meeting.builder()
                .coupleId(coupleId)
                .name(tmpMeeting.getName())
                .startTime(startDateTime)
                .endTime(endDateTime)
                .date(tmpMeeting.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .build();
    }
    
    /**
     * TmpMeeting의 routes를 Route 엔티티로 저장
     */
    private void saveRouteFromTmpMeeting(TmpMeeting tmpMeeting, UUID meetingId) {
        if (tmpMeeting.getResults() != null && tmpMeeting.getResults().getRoutes() != null) {
            // routes 값을 그대로 저장
            Route route = Route.builder()
                    .routes(tmpMeeting.getResults().getRoutes())
                    .build();
            
            Route savedRoute = routeRepository.save(route);
            log.info("Route 저장 성공: routeId={}", savedRoute.getId());
        }
    }
    
    /**
     * TmpMeeting의 stores를 MeetingPlaces로 저장
     */
    private void saveMeetingPlacesFromTmpMeeting(TmpMeeting tmpMeeting, UUID meetingId) {
        if (tmpMeeting.getStores() != null && !tmpMeeting.getStores().isEmpty()) {
            List<MeetingPlace> meetingPlaces = tmpMeeting.getStores().stream()
                    .map(storeName -> MeetingPlace.builder()
                            .meetingId(meetingId)
                            .name(storeName)
                            .sequence(tmpMeeting.getStores().indexOf(storeName) + 1)
                            .build())
                    .collect(Collectors.toList());
            
            meetingPlaceRepository.saveAll(meetingPlaces);
            log.info("MeetingPlaces 저장 성공: {}개", meetingPlaces.size());
        }
    }
    
    /**
     * TmpMeeting의 keywords를 MeetingKeywords로 저장
     */
    private void saveMeetingKeywordsFromTmpMeeting(TmpMeeting tmpMeeting, UUID meetingId) {
        if (tmpMeeting.getKeyword() != null && !tmpMeeting.getKeyword().isEmpty()) {
            List<MeetingKeyword> meetingKeywords = tmpMeeting.getKeyword().stream()
                    .map(keywordContent -> MeetingKeyword.builder()
                            .meetingId(meetingId)
                            .keyword(keywordContent)
                            .sequence(tmpMeeting.getKeyword().indexOf(keywordContent) + 1)
                            .build())
                    .collect(Collectors.toList());
            
            meetingKeywordRepository.saveAll(meetingKeywords);
            log.info("MeetingKeywords 저장 성공: {}개", meetingKeywords.size());
        }
    }
    

} 