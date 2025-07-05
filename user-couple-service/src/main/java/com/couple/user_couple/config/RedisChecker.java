package com.couple.user_couple.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisChecker {

    private final RedisConnectionFactory factory;

    @PostConstruct
    public void checkRedisConfig() {
        if (factory instanceof LettuceConnectionFactory lettuce) {
            System.out.println("✅ Redis Host: " + lettuce.getHostName());
            System.out.println("✅ Redis Port: " + lettuce.getPort());
        } else {
            System.out.println("⚠️ RedisConnectionFactory is not a LettuceConnectionFactory: " + factory.getClass());
        }
    }
}
