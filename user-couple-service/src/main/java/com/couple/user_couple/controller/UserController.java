package com.couple.user_couple.controller;

import com.couple.common.dto.ApiResponse;
import com.couple.user_couple.dto.UserSignupRequest;
import com.couple.user_couple.dto.UserUpdateRequest;
import com.couple.user_couple.dto.UserResponse;
import com.couple.user_couple.dto.LoginRequest;
import com.couple.user_couple.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> signup(
            @Valid @RequestBody UserSignupRequest request) {
        log.info("회원가입 요청: {}", request.getName());

        UserResponse response = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 사용자의 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(
            @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") @RequestHeader("X-User-ID") String userId) {

        UserResponse response = userService.getUserInfo(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회 성공", response));
    }

    @PutMapping("/me")
    @Operation(summary = "내 정보 수정", description = "현재 사용자의 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyInfo(
            @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody UserUpdateRequest request) {

        UserResponse response = userService.updateUserInfo(UUID.fromString(userId), request);
        return ResponseEntity.ok(ApiResponse.success("사용자 정보 수정 성공", response));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("로그인 요청: {}", request.getEmail());

        UserResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", response));
    }

    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "현재 사용자를 삭제합니다.")
    public ResponseEntity<ApiResponse<String>> deleteMyAccount(
            @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000") @RequestHeader("X-User-ID") String userId) {

        userService.deleteUser(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다.", null));
    }
}