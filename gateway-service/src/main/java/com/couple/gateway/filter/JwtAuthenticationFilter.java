package com.couple.gateway.filter;

import com.couple.common.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private ApplicationContext applicationContext;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // 인증이 필요하지 않은 경로 체크
            String path = request.getPath().value();
            if (isPublicPath(path)) {
                return chain.filter(exchange);
            }

            // Authorization 헤더에서 JWT 토큰 추출
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("JWT 토큰이 없습니다. 경로: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            try {
                // ApplicationContext를 통해 JwtTokenProvider 가져오기
                JwtTokenProvider jwtTokenProvider = applicationContext.getBean(JwtTokenProvider.class);

                // JWT 토큰 검증
                if (!jwtTokenProvider.validateToken(token)) {
                    log.warn("유효하지 않은 JWT 토큰입니다. 경로: {}", path);
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                // 사용자 ID와 커플 ID 추출
                UUID userId = jwtTokenProvider.getUserIdFromToken(token);
                UUID coupleId = jwtTokenProvider.getCoupleIdFromToken(token);

                // 헤더에 사용자 정보 추가
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-ID", userId.toString())
                        .header("X-Couple-ID", coupleId != null ? coupleId.toString() : "null")
                        .build();

                log.debug("JWT 인증 성공. 사용자 ID: {}, 커플 ID: {}, 경로: {}", userId, coupleId, path);
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                log.error("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    private boolean isPublicPath(String path) {
        // 인증이 필요하지 않은 공개 경로들
        return path.startsWith("/api/auth/") ||
                path.startsWith("/users/signup") ||
                path.startsWith("/users/login") ||
                path.startsWith("/api/questions/test-headers") ||
                path.startsWith("/actuator/") ||
                path.equals("/health") ||
                path.equals("/info");
    }

    public static class Config {
        // 설정이 필요한 경우 여기에 추가
    }
}