package com.couple.user_couple.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다")
    private String name;

    @NotBlank(message = "FCM 코드는 필수입니다")
    @Size(max = 50, message = "FCM 코드는 50자 이하여야 합니다")
    private String fcmCode;

    @NotNull(message = "푸시 알림 시간은 필수입니다")
    private LocalDateTime sendTime;
}