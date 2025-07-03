package com.couple.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-couple-service", r -> r
                        .path("/api/users/**", "/api/couples/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")
                                .addRequestHeader("X-Response-Time", System.currentTimeMillis() + ""))
                        .uri("http://localhost:8081"))
                .route("schedule-meeting-service", r -> r
                        .path("/api/schedules/**", "/api/meetings/**", "/api/places/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")
                                .addRequestHeader("X-Response-Time", System.currentTimeMillis() + ""))
                        .uri("http://localhost:8082"))
                .route("chat-push-service", r -> r
                        .path("/api/chat/**", "/api/push/**", "/api/questions/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")
                                .addRequestHeader("X-Response-Time", System.currentTimeMillis() + ""))
                        .uri("http://localhost:8083"))
                .route("websocket", r -> r
                        .path("/ws/**")
                        .filters(f -> f
                                .rewritePath("/ws/(?<segment>.*)", "/${segment}"))
                        .uri("ws://localhost:8083"))
                .build();
    }
}