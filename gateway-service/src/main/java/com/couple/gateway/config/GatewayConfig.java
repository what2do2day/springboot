package com.couple.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.client.RestTemplate;
import com.couple.gateway.filter.JwtAuthenticationFilter;

@Configuration
public class GatewayConfig {

        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder, JwtAuthenticationFilter jwtFilter) {
                return builder.routes()
                                .route("user-couple-service-public", r -> r
                                                .path("/api/users/signup", "/api/users/login")
                                                .filters(f -> f
                                                                .rewritePath("/api/(?<segment>.*)", "/api/${segment}")
                                                                .addRequestHeader("X-Response-Time",
                                                                                System.currentTimeMillis() + ""))
                                                .uri("http://localhost:8081"))
                                .route("user-couple-service", r -> r
                                                .path("/api/users/**", "/api/couples/**")
                                                .filters(f -> f
                                                                .rewritePath("/api/(?<segment>.*)", "/api/${segment}")
                                                                .addRequestHeader("X-Response-Time",
                                                                                System.currentTimeMillis() + "")
                                                                .filter(jwtFilter.apply(
                                                                                new JwtAuthenticationFilter.Config())))
                                                .uri("http://localhost:8081"))
                                .route("schedule-meeting-service", r -> r
                                                .path("/api/schedules/**", "/api/meetings/**", "/api/places/**")
                                                .filters(f -> f
                                                                .rewritePath("/api/(?<segment>.*)", "/api/${segment}")
                                                                .addRequestHeader("X-Response-Time",
                                                                                System.currentTimeMillis() + "")
                                                                .filter(jwtFilter.apply(
                                                                                new JwtAuthenticationFilter.Config())))
                                                .uri("http://localhost:8082"))
                                .route("chat-push-service", r -> r
                                                .path("/api/chat/**", "/api/push/**", "/api/questions/**")
                                                .filters(f -> f
                                                                .rewritePath("/api/(?<segment>.*)", "/api/${segment}")
                                                                .addRequestHeader("X-Response-Time",
                                                                                System.currentTimeMillis() + "")
                                                                .filter(jwtFilter.apply(
                                                                                new JwtAuthenticationFilter.Config())))
                                                .uri("http://localhost:8083"))
                                .route("websocket", r -> r
                                                .path("/ws/**")
                                                .filters(f -> f
                                                                .rewritePath("/ws/(?<segment>.*)", "/${segment}"))
                                                .uri("ws://localhost:8083"))
                                .build();
        }

        @Bean
        public CorsWebFilter corsWebFilter() {
                CorsConfiguration corsConfig = new CorsConfiguration();
                corsConfig.setAllowedOriginPatterns(java.util.List.of("*"));
                corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                corsConfig.setAllowedHeaders(java.util.List.of("*"));
                corsConfig.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", corsConfig);

                return new CorsWebFilter(source);
        }

        @Bean
        public RestTemplate restTemplate() {
                return new RestTemplate();
        }
}