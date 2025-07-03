package com.couple.user_couple.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private String name;
    private UUID coupleId;
    private Long score;
    private String gender;
    private String sendTime;
    private String fcmCode;
    private String birth;
    private Integer year;
    private Integer month;
    private Integer date;
    private String accessToken;
    private String refreshToken;
}