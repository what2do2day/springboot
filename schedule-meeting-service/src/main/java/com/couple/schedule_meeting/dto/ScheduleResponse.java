package com.couple.schedule_meeting.dto;

import com.couple.schedule_meeting.entity.Schedule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponse {
    private UUID id;
    private String name;
    private String message;
    private LocalDateTime dateTime;

    public static ScheduleResponse from(Schedule schedule) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .name(schedule.getName())
                .message(schedule.getMessage())
                .dateTime(schedule.getDateTime())
                .build();
    }
} 