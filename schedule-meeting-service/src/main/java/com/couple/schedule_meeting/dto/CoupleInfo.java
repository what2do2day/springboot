package com.couple.schedule_meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoupleInfo {
    private String coupleId;
    private UserInfo user1;
    private UserInfo user2;
    // 커플의 기본 정보와 취향 벡터를 모두 포함
} 