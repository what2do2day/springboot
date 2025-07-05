package com.couple.gateway.service;

import com.couple.common.security.JwtTokenProvider;
import com.couple.gateway.dto.LoginRequest;
import com.couple.gateway.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;

    public LoginResponse login(LoginRequest request) {
        log.info("Gateway에서 로그인 요청 처리: {}", request.getEmail());

        // User-Couple Service에 로그인 요청
        String userServiceUrl = "http://user-couple-service:8081/api/users/login";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<LoginRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                userServiceUrl,
                HttpMethod.POST,
                entity,
                Map.class);

        Map<String, Object> userData = (Map<String, Object>) response.getBody().get("data");
        String userId = userData.get("id").toString();
        String name = userData.get("name").toString();
        String email = userData.get("email").toString();

        // User-Couple Service에서 생성된 토큰을 그대로 사용
        String accessToken = userData.get("accessToken").toString();

        log.info("User-Couple Service에서 받은 토큰: {}", accessToken);
        log.info("토큰 길이: {}", accessToken.length());
        log.info("토큰이 ey로 시작하는지: {}", accessToken.startsWith("ey"));

        return new LoginResponse(accessToken, userId, name, email);
    }

    public String validateToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return jwtTokenProvider.getUserIdFromToken(token).toString();
    }
}