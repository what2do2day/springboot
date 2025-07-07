package com.couple.mission_store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionResponse {

    private UUID id;
    private String title;
    private String description;
    private Integer coinReward;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}