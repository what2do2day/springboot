package com.couple.user_couple.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoupleMatchRequest {

    private String name;
    private java.time.LocalDate startDate;
}