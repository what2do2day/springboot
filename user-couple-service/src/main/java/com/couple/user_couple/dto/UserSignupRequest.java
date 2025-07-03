package com.couple.user_couple.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이하여야 합니다")
    private String password;

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다")
    private String name;

    @NotBlank(message = "성별은 필수입니다")
    @Pattern(regexp = "^[MF]$", message = "성별은 M 또는 F여야 합니다")
    private String gender;

    @NotNull(message = "생년월일은 필수입니다")
    @Past(message = "생년월일은 과거 날짜여야 합니다")
    private LocalDate birth;

    @NotBlank(message = "FCM 코드는 필수입니다")
    @Size(max = 50, message = "FCM 코드는 50자 이하여야 합니다")
    private String fcmCode;

    @NotNull(message = "푸시 알림 시간은 필수입니다")
    private LocalTime sendTime;
}