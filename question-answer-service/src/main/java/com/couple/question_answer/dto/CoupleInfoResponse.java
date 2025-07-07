package com.couple.question_answer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoupleInfoResponse {

    private UUID coupleId;
    private String coupleName;
    private Long dday;
    private String startDate;
    private UserInfo user1;
    private UserInfo user2;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private UUID userId;
        private String name;
        private String gender;
        private String birth;
    }
} 