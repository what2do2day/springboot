package com.couple.gateway.filter;

import com.couple.common.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.UUID;

@Slf4j
@Component
public class WebSocketAuthFilter extends AbstractGatewayFilterFactory<WebSocketAuthFilter.Config> {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public WebSocketAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            // WebSocket 연결 경로인지 확인
            if (path.startsWith("/ws")) {
                String token = extractTokenFromQuery(request);

                if (token != null && jwtTokenProvider.validateToken(token)) {
                    UUID userId = jwtTokenProvider.getUserIdFromToken(token);
                    UUID coupleId = jwtTokenProvider.getCoupleIdFromToken(token);

                    // 사용자 정보를 헤더에 추가 (닉네임은 유저 ID와 동일하게 설정)
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-ID", userId.toString())
                            .header("X-Couple-ID", coupleId != null ? coupleId.toString() : "")
                            .header("X-Nickname", userId.toString())
                            .header("X-Email", userId.toString() + "@example.com")
                            .build();

                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(modifiedRequest)
                            .build();

                    log.info("WebSocket 인증 성공. 사용자 ID: {}, 닉네임: {}, 커플 ID: {}, 경로: {}", userId, userId, coupleId,
                            path);
                    return chain.filter(modifiedExchange);
                } else {
                    log.warn("WebSocket 인증 실패. 경로: {}", path);
                    return chain.filter(exchange);
                }
            }

            return chain.filter(exchange);
        };
    }

    private String extractTokenFromQuery(ServerHttpRequest request) {
        String token = request.getQueryParams().getFirst("token");
        if (token != null) {
            return token;
        }

        // Authorization 헤더에서도 확인
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    public static class Config {
        // 설정이 필요한 경우 여기에 추가
    }
}