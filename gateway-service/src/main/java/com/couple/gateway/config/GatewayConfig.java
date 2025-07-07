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
import com.couple.gateway.filter.WebSocketAuthFilter;

@Configuration
public class GatewayConfig {

        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder, JwtAuthenticationFilter jwtFilter,
                        WebSocketAuthFilter webSocketAuthFilter) {
                return builder.routes()
                                .route("user-couple-service-public", r -> r
                                                .path("/api/users/signup", "/api/users/login")
                                                .filters(f -> f
                                                                .rewritePath("/api/(?<segment>.*)", "/api/${segment}")
                                                                .addRequestHeader("X-Response-Time",
                                                                                System.currentTimeMillis() + ""))
                                                .uri("http://user-couple-service:8081"))
                                .route("user-couple-service", r -> r
                                                .path("/api/users/**", "/api/couples/**")
                                                .filters(f -> f
                                                                .rewritePath("/api/(?<segment>.*)", "/api/${segment}")
                                                                .addRequestHeader("X-Response-Time",
                                                                                System.currentTimeMillis() + "")
                                                                .filter(jwtFilter.apply(
                                                                                new JwtAuthenticationFilter.Config())))
                                                .uri("http://user-couple-service:8081"))
                                .route("schedule-meeting-service", r -> r
                                                .path("/api/schedules/**", "/api/meetings/**", "/api/places/**")
                                                .filters(f -> f
                                                                .rewritePath("/api/(?<segment>.*)", "/api/${segment}")
                                                                .addRequestHeader("X-Response-Time",
                                                                                System.currentTimeMillis() + "")
                                                                .filter(jwtFilter.apply(
                                                                                new JwtAuthenticationFilter.Config())))
                                                .uri("http://schedule-meeting-service:8082"))
                                .route("question-answer-service", r -> r
                                                .path("/api/questions/**", "/api/tags/**", "/api/user-answers/**",
                                                                "/api/user-tag-profiles/**")
                                                .filters(f -> f
                                                                .rewritePath("/api/(?<segment>.*)", "/api/${segment}")
                                                                .addRequestHeader("X-Response-Time",
                                                                                System.currentTimeMillis() + "")
                                                                .filter(jwtFilter.apply(
                                                                                new JwtAuthenticationFilter.Config())))
                                                .uri("http://question-answer-service:8086"))
                                .route("couple-chat-service", r -> r
                                                .path("/api/couple-chat/**")
                                                .filters(f -> f
                                                                .rewritePath("/api/(?<segment>.*)", "/api/${segment}")
                                                                .addRequestHeader("X-Response-Time",
                                                                                System.currentTimeMillis() + "")
                                                                .filter(jwtFilter.apply(
                                                                                new JwtAuthenticationFilter.Config())))
                                                .uri("http://couple-chat-service:8084"))
                                .route("couple-chat-location", r -> r
                                                .path("/api/location/**")
                                                .filters(f -> f
                                                                .rewritePath("/api/(?<segment>.*)", "/api/${segment}")
                                                                .addRequestHeader("X-Response-Time",
                                                                                System.currentTimeMillis() + "")
                                                                .filter(jwtFilter.apply(
                                                                                new JwtAuthenticationFilter.Config())))
                                                .uri("http://couple-chat-service:8084"))
                                .route("couple-chat-websocket-api", r -> r
                                                .path("/api/websocket/**")
                                                .filters(f -> f
                                                                .rewritePath("/api/(?<segment>.*)", "/api/${segment}")
                                                                .addRequestHeader("X-Response-Time",
                                                                                System.currentTimeMillis() + "")
                                                                .filter(jwtFilter.apply(
                                                                                new JwtAuthenticationFilter.Config())))
                                                .uri("http://couple-chat-service:8084"))
                                .route("couple-chat-websocket", r -> r
                                                .path("/ws/connect/**")
                                                .filters(f -> f
                                                                .rewritePath("/ws/connect/(?<segment>.*)", "/ws/connect")
                                                                .filter(webSocketAuthFilter.apply(
                                                                                new WebSocketAuthFilter.Config())))
                                                .uri("ws://couple-chat-service:8084"))

                                .route("couple-chat-static", r -> r
                                                .path("/", "/index.html", "/js/**", "/css/**")
                                                .uri("http://couple-chat-service:8084"))
                                .route("mission-store-service", r -> r
                                                .path("/api/missions/**", "/api/shop/**")
                                                .filters(f -> f
                                                                .rewritePath("/api/(?<segment>.*)", "/api/${segment}")
                                                                .addRequestHeader("X-Response-Time",
                                                                                System.currentTimeMillis() + "")
                                                                .filter(jwtFilter.apply(
                                                                                new JwtAuthenticationFilter.Config())))
                                                .uri("http://mission-store-service:8088"))
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