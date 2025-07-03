package com.couple.schedule_meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    private String description;

    @NotNull(message = "시작 시간은 필수입니다.")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer budget;

    private String meetingType;

    private List<MeetingPlaceRequest> places;
}