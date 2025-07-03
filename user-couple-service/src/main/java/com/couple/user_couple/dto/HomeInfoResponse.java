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
public class HomeInfoResponse {

    private UUID coupleId;
    private String coupleName;
    private String characterId;
    private Long daysSinceStart;
    private UserResponse partner;
}