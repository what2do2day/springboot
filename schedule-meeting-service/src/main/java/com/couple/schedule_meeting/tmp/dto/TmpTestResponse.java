package com.couple.schedule_meeting.tmp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmpTestResponse {
    private String id;
    private String text;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 