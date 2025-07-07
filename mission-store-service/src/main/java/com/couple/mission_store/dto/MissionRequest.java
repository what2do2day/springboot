package com.couple.mission_store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionRequest {

    @NotBlank(message = "미션 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "미션 설명은 필수입니다.")
    private String description;

    @NotNull(message = "보상 코인은 필수입니다.")
    @Positive(message = "보상 코인은 양수여야 합니다.")
    @Builder.Default
    private Integer coinReward = 10;
}