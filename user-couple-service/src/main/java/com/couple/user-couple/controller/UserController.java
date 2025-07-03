package com.couple.user_couple.controller;

import com.couple.common.dto.ApiResponse;
import com.couple.user_couple.dto.UserSignupRequest;
import com.couple.user_couple.entity.User;
import com.couple.user_couple.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public ResponseEntity<ApiResponse<User>> signup(@Valid @RequestBody UserSignupRequest request) {
        User user = userService.signup(request);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", user));
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<User>> getMyInfo(HttpServletRequest request) {
        String userId = request.getHeader("X-User-ID");
        User user = userService.getUserById(Long.parseLong(userId));
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @DeleteMapping("/me")
    @Operation(summary = "회원탈퇴", description = "현재 로그인한 사용자를 삭제합니다.")
    public ResponseEntity<ApiResponse<String>> deleteUser(HttpServletRequest request) {
        String userId = request.getHeader("X-User-ID");
        userService.deleteUser(Long.parseLong(userId));
        return ResponseEntity.ok(ApiResponse.success("회원탈퇴가 완료되었습니다.", null));
    }
}