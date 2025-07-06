package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.*;
import com.couple.schedule_meeting.entity.Meeting;
import com.couple.schedule_meeting.entity.Schedule;
import com.couple.schedule_meeting.exception.ScheduleAccessDeniedException;
import com.couple.schedule_meeting.exception.ScheduleNotFoundException;
import com.couple.schedule_meeting.repository.MeetingRepository;
import com.couple.schedule_meeting.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final MeetingRepository meetingRepository;
    private final UserProfileService userProfileService;
    private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);

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
    public Schedule getScheduleById(UUID scheduleId, UUID coupleId) {
        return findScheduleWithPermission(scheduleId, coupleId);
    }

    @Transactional
    public Schedule updateSchedule(UUID scheduleId, ScheduleUpdateRequest request, UUID coupleId) {
        Schedule existingSchedule = findScheduleWithPermission(scheduleId, coupleId);
        
        Schedule updatedSchedule = Schedule.builder()
                .id(existingSchedule.getId())
                .coupleId(existingSchedule.getCoupleId())
                .userId(existingSchedule.getUserId())
                .name(getValueOrDefault(request.getName(), existingSchedule.getName()))
                .message(getValueOrDefault(request.getMessage(), existingSchedule.getMessage()))
                .dateTime(getValueOrDefault(request.getDateTime(), existingSchedule.getDateTime()))
                .build();
        
        return scheduleRepository.save(updatedSchedule);
    }

    @Transactional
    public void deleteSchedule(UUID scheduleId, UUID coupleId) {
        Schedule schedule = findScheduleWithPermission(scheduleId, coupleId);
        scheduleRepository.delete(schedule);
    }

    /**
     * 일정을 조회하고 권한을 확인하는 공통 메서드
     */
    private Schedule findScheduleWithPermission(UUID scheduleId, UUID coupleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
        
        // 권한 확인: 일정이 해당 커플에 속하는지 확인
        if (!schedule.getCoupleId().equals(coupleId)) {
            throw new ScheduleAccessDeniedException(scheduleId, coupleId);
        }
        
        return schedule;
    }

    /**
     * null이 아닌 경우 새로운 값을, null인 경우 기본값을 반환하는 유틸리티 메서드
     */
    private <T> T getValueOrDefault(T newValue, T defaultValue) {
        return newValue != null ? newValue : defaultValue;
    }

    @Transactional(readOnly = true)
    public ScheduleCalendarResponse getCalendar(UUID coupleId, int year, int month, String userId) {
        List<ScheduleCalendarResponse.DateInfo> dateInfos = new ArrayList<>();

        // 1. 미팅
        dateInfos.addAll(getMeetingDateInfos(coupleId, year, month));

        // 2. 커플 멤버 생일
        dateInfos.addAll(getBirthdayDateInfos(userId, year, month));

        // 3. 기념일 (역산 방식)
        dateInfos.addAll(getDdayAnniversaryInfos(userId, year, month));

        // 날짜순 정렬
        dateInfos = dateInfos.stream()
                .sorted(Comparator.comparingInt(ScheduleCalendarResponse.DateInfo::getDate))
                .collect(Collectors.toList());

        return new ScheduleCalendarResponse(year, month, dateInfos);
    }

    private List<ScheduleCalendarResponse.DateInfo> getMeetingDateInfos(UUID coupleId, int year, int month) {
        List<ScheduleCalendarResponse.DateInfo> result = new ArrayList<>();
        List<Meeting> meetings = meetingRepository.findByCoupleIdAndYearAndMonthOrderByStartTimeAsc(coupleId, year, month);
        for (Meeting meeting : meetings) {
            if (meeting.getDay() != null) {
                result.add(new ScheduleCalendarResponse.DateInfo(
                        meeting.getDay(),
                        "meeting",
                        meeting.getName(),
                        meeting.getId().toString()
                ));
            }
        }
        return result;
    }

    private List<ScheduleCalendarResponse.DateInfo> getBirthdayDateInfos(String userId, int year, int month) {
        List<ScheduleCalendarResponse.DateInfo> result = new ArrayList<>();
        
        try {
            log.info("생일 정보 조회 시작: userId={}, year={}, month={}", userId, year, month);
            
            // userId로 커플 정보 조회
            CoupleInfoResponse coupleInfo = userProfileService.getCoupleInfoByUserId(userId);
            log.info("커플 정보 조회 결과: coupleInfo={}", coupleInfo);
            
            if (coupleInfo != null && coupleInfo.getCoupleId() != null && coupleInfo.getUser1() != null && coupleInfo.getUser2() != null) {
                log.info("user1 생일: {}, user2 생일: {}", 
                        coupleInfo.getUser1().getBirth(), coupleInfo.getUser2().getBirth());
                log.info("user1 이름: {}, user2 이름: {}", 
                        coupleInfo.getUser1().getName(), coupleInfo.getUser2().getName());
                
                // 각 사용자의 생일 처리
                addUserBirthdayToCalendar(coupleInfo.getUser1(), "user1", month, result);
                addUserBirthdayToCalendar(coupleInfo.getUser2(), "user2", month, result);
            } else {
                log.warn("커플 정보가 null이거나 사용자 정보가 부족함");
            }
            
            log.info("생일 정보 조회 완료: {}개 생일 발견", result.size());
            
        } catch (RuntimeException e) {
            log.warn("커플 정보 조회 실패: userId={}, error={}", userId, e.getMessage());
        } catch (Exception e) {
            log.warn("커플 정보 조회 오류: userId={}", userId, e);
        }
        return result;
    }

    private List<ScheduleCalendarResponse.DateInfo> getDdayAnniversaryInfos(String userId, int year, int month) {
        List<ScheduleCalendarResponse.DateInfo> result = new ArrayList<>();
        
        try {
            // 1. 커플 시작일 조회
            CoupleInfoResponse coupleInfo = userProfileService.getCoupleInfoByUserId(userId);
            
            // 커플 정보가 null이거나 startDate가 null인 경우 처리
            if (coupleInfo == null || coupleInfo.getStartDate() == null) {
                log.warn("커플 정보가 null이거나 시작일이 없음: userId={}", userId);
                return result;
            }
            
            LocalDate startDate = LocalDateTime.parse(coupleInfo.getStartDate()).toLocalDate();
            
            // 2. 해당 월의 첫날과 마지막날
            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            
            // 3. 역산으로 기념일 찾기
            // 100일 단위 기념일 (100일 ~ 1000일)
            for (int days = 100; days <= 1000; days += 100) {
                LocalDate anniversary = startDate.plusDays(days);
                if (!anniversary.isBefore(monthStart) && !anniversary.isAfter(monthEnd)) {
                    result.add(new ScheduleCalendarResponse.DateInfo(
                        anniversary.getDayOfMonth(),
                        "anniversary_days",
                        days + "일",
                        null
                    ));
                }
            }
            
            // 4. 연 단위 기념일 (1년 ~ 10년)
            for (int years = 1; years <= 10; years++) {
                LocalDate anniversary = startDate.plusYears(years);
                if (!anniversary.isBefore(monthStart) && !anniversary.isAfter(monthEnd)) {
                    result.add(new ScheduleCalendarResponse.DateInfo(
                        anniversary.getDayOfMonth(),
                        "anniversary_years",
                        years + "년",
                        null
                    ));
                }
            }
            
        } catch (RuntimeException e) {
            log.warn("기념일 계산 실패: userId={}, error={}", userId, e.getMessage());
        } catch (Exception e) {
            log.warn("기념일 계산 오류: userId={}", userId, e);
        }
        
        return result;
    }
    
    private void addUserBirthdayToCalendar(CoupleInfoResponse.UserInfo user, String userLabel, int month, List<ScheduleCalendarResponse.DateInfo> result) {
        if (user.getBirth() != null && !user.getBirth().isEmpty()) {
            try {
                LocalDate birthDate = LocalDate.parse(user.getBirth(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                log.info("{} 생일 파싱 결과: {}", userLabel, birthDate);
                
                if (birthDate.getMonthValue() == month) {
                    log.info("{} 생일이 해당 월에 속함: {}", userLabel, birthDate.getDayOfMonth());
                    result.add(new ScheduleCalendarResponse.DateInfo(
                            birthDate.getDayOfMonth(),
                            "birthday",
                            user.getName(),
                            user.getUserId().toString()
                    ));
                } else {
                    log.info("{} 생일이 해당 월에 속하지 않음: {}월 != {}월", userLabel, birthDate.getMonthValue(), month);
                }
            } catch (Exception e) {
                log.warn("{} 생일 날짜 파싱 오류: userId={}, birth={}", 
                        userLabel, user.getUserId(), user.getBirth(), e);
            }
        } else {
            log.info("{} 생일 정보가 없음", userLabel);
        }
    }

} 