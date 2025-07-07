package com.couple.couple_chat.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationShareRequest {

    @NotNull(message = "방 ID는 필수입니다")
    private UUID roomId;

    @NotNull(message = "위도는 필수입니다")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다")
    private Double longitude;

    private Double accuracy; // 위치 정확도 (미터)

    private String address; // 주소 정보

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Builder.Default
    private String messageType = "LOCATION"; // 위치 공유 메시지 타입
} 