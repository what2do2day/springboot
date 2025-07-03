package com.couple.user_couple.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoupleMatchAcceptRequest {

    @NotBlank(message = "매칭 코드는 필수입니다")
    private String matchingCode;
}