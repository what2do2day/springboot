package com.couple.user_couple.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoupleDateRequest {

    @NotNull(message = "날짜는 필수입니다")
    private LocalDate date;
}