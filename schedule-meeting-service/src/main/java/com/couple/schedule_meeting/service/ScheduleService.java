package com.couple.schedule_meeting.service;

import com.couple.schedule_meeting.dto.*;
import com.couple.schedule_meeting.entity.Schedule;
import com.couple.schedule_meeting.exception.ScheduleAccessDeniedException;
import com.couple.schedule_meeting.exception.ScheduleNotFoundException;
import com.couple.schedule_meeting.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.couple.schedule_meeting.entity.Meeting;
import com.couple.schedule_meeting.repository.MeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    @Autowired
    private MeetingRepository meetingRepository;
    @Autowired
    private UserProfileService userProfileService;

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

    public ScheduleCalendarResponse getCalendar(UUID coupleId, int year, int month) {
        List<ScheduleCalendarResponse.DateInfo> dateInfos = new ArrayList<>();

        // 1. 미팅
        dateInfos.addAll(getMeetingDateInfos(coupleId, year, month));

        // 2. 행사
        dateInfos.addAll(getScheduleDateInfos(coupleId, year, month));

        // 3. 커플 멤버 생일
        dateInfos.addAll(getBirthdayDateInfos(coupleId, year, month));

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
                        null
                ));
            }
        }
        return result;
    }

    private List<ScheduleCalendarResponse.DateInfo> getScheduleDateInfos(UUID coupleId, int year, int month) {
        List<ScheduleCalendarResponse.DateInfo> result = new ArrayList<>();
        List<Schedule> schedules = scheduleRepository.findByCoupleIdAndYearAndMonthOrderByDayAsc(coupleId, year, month);
        for (Schedule schedule : schedules) {
            if (schedule.getDay() != null) {
                result.add(new ScheduleCalendarResponse.DateInfo(
                        schedule.getDay(),
                        "schedule",
                        schedule.getName(),
                        null
                ));
            }
        }
        return result;
    }

    private List<ScheduleCalendarResponse.DateInfo> getBirthdayDateInfos(UUID coupleId, int year, int month) {
        List<ScheduleCalendarResponse.DateInfo> result = new ArrayList<>();
        // coupleId로 userId 1개를 임의로 얻기 위해 행사에서 userId 추출
        List<Schedule> schedules = scheduleRepository.findByCoupleIdAndYearAndMonthOrderByDayAsc(coupleId, year, month);
        String userId = null;
        if (!schedules.isEmpty()) {
            userId = schedules.get(0).getUserId().toString();
        }
        if (userId != null) {
            CoupleProfile coupleProfile = userProfileService.getCoupleUserProfilesByUserId(userId);
            List<UserProfile> users = Arrays.asList(coupleProfile.getUser1(), coupleProfile.getUser2());
            for (int i = 0; i < users.size(); i++) {
                UserProfile user = users.get(i);
                if (user.getBirth() != null && !user.getBirth().isEmpty()) {
                    LocalDate birthDate = LocalDate.parse(user.getBirth(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    if (birthDate.getMonthValue() == month) {
                        result.add(new ScheduleCalendarResponse.DateInfo(
                                birthDate.getDayOfMonth(),
                                i == 0 ? "user1_birthday" : "user2_birthday",
                                null,
                                user.getId()
                        ));
                    }
                }
            }
        }
        return result;
    }
} 