package com.couple.user_couple.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoupleResponse {

    private UUID id;
    private String name;
    private String characterId;
    private LocalDateTime startDate;
    private LocalDate customDate;
    private Integer year;
    private Integer month;
    private Integer date;
    private LocalDateTime expiredAt;
    private String expired;
    private Long daysSinceStart;
}