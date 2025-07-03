package com.couple.gateway.controller;

import com.couple.common.dto.ApiResponse;
import com.couple.gateway.dto.LoginRequest;
import com.couple.gateway.dto.LoginResponse;
import com.couple.gateway.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("로그인 요청: {}", request.getEmail());

        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", response));
    }

    @PostMapping("/validate")
    @Operation(summary = "토큰 검증", description = "JWT 토큰의 유효성을 검증합니다.")
    public ResponseEntity<ApiResponse<Map<String, String>>> validateToken(
            @RequestHeader("Authorization") String token) {
        log.info("토큰 검증 요청");

        String userId = authService.validateToken(token);
        Map<String, String> response = new HashMap<>();
        response.put("userId", userId);

        return ResponseEntity.ok(ApiResponse.success("토큰이 유효합니다.", response));
    }
}