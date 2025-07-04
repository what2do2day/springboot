package com.couple.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnly}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(UUID userId, UUID coupleId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        log.info("JWT 토큰 생성 시작 - userId: {}, coupleId: {}", userId, coupleId);

        JwtBuilder builder = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey());

        // coupleId를 항상 claim에 추가 (null이어도 "null" 문자열로 저장)
        builder.claim("coupleId", coupleId != null ? coupleId.toString() : "null");

        String token = builder.compact();

        log.info("생성된 JWT 토큰: {}", token);
        log.info("토큰 길이: {}", token.length());
        log.info("토큰이 ey로 시작하는지: {}", token.startsWith("ey"));

        return token;
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public UUID getCoupleIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String coupleIdStr = claims.get("coupleId", String.class);
        return coupleIdStr != null && !coupleIdStr.equals("null") ? UUID.fromString(coupleIdStr) : null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }
}