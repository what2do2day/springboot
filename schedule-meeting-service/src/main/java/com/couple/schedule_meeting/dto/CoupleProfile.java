package com.couple.schedule_meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoupleProfile {
    private String coupleId;
    private UserProfile user1;
    private UserProfile user2;
    // 커플 정보
} 