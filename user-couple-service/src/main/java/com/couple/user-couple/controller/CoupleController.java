package com.couple.user_couple.controller;

import com.couple.common.dto.ApiResponse;
import com.couple.user_couple.dto.CoupleMatchRequest;
import com.couple.user_couple.entity.Couple;
import com.couple.user_couple.service.CoupleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/couples")
@RequiredArgsConstructor
@Tag(name = "Couple", description = "커플 관련 API")
public class CoupleController {

    private final CoupleService coupleService;

    @PostMapping("/match-code")
    @Operation(summary = "매칭 코드 생성", description = "커플 매칭을 위한 코드를 생성합니다.")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateMatchCode(HttpServletRequest request) {
        String userId = request.getHeader("X-User-ID");
        String matchCode = coupleService.generateMatchCode(Long.parseLong(userId));

        Map<String, String> response = new HashMap<>();
        response.put("matchCode", matchCode);

        return ResponseEntity.ok(ApiResponse.success("매칭 코드가 생성되었습니다.", response));
    }

    @PostMapping("/match")
    @Operation(summary = "커플 매칭", description = "매칭 코드를 사용하여 커플을 매칭합니다.")
    public ResponseEntity<ApiResponse<Couple>> matchCouple(
            HttpServletRequest request,
            @Valid @RequestBody CoupleMatchRequest matchRequest) {
        String userId = request.getHeader("X-User-ID");
        Couple couple = coupleService.matchCouple(Long.parseLong(userId), matchRequest);
        return ResponseEntity.ok(ApiResponse.success("커플 매칭이 완료되었습니다.", couple));
    }

    @GetMapping("/me")
    @Operation(summary = "내 커플 정보 조회", description = "현재 사용자의 커플 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<Couple>> getMyCouple(HttpServletRequest request) {
        String userId = request.getHeader("X-User-ID");
        Couple couple = coupleService.getCoupleByUserId(Long.parseLong(userId));
        return ResponseEntity.ok(ApiResponse.success(couple));
    }

    @DeleteMapping("/me")
    @Operation(summary = "커플 파기", description = "현재 커플 관계를 해제합니다.")
    public ResponseEntity<ApiResponse<String>> breakUpCouple(HttpServletRequest request) {
        String userId = request.getHeader("X-User-ID");
        coupleService.breakUpCouple(Long.parseLong(userId));
        return ResponseEntity.ok(ApiResponse.success("커플 관계가 해제되었습니다.", null));
    }
}