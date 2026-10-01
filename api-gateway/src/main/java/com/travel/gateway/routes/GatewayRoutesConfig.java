package com.travel.gateway.routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${AUTH_SERVICE_URL:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${TOUR_SERVICE_URL:http://localhost:8082}")
    private String tourServiceUrl;

    @Value("${BOOKING_SERVICE_URL:http://localhost:8083}")
    private String bookingServiceUrl;

    @Value("${PAYMENT_SERVICE_URL:http://localhost:8084}")
    private String paymentServiceUrl;

    @Value("${AI_SERVICE_URL:http://localhost:8085}")
    private String aiServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .uri(authServiceUrl))
                .route("tour-service", r -> r.path("/api/v1/tours/**", "/api/v1/destinations/**", "/api/v1/schedules/**")
                        .uri(tourServiceUrl))
                .route("booking-service", r -> r.path("/api/v1/bookings/**")
                        .uri(bookingServiceUrl))
                .route("payment-service", r -> r.path("/api/v1/payments/**")
                        .uri(paymentServiceUrl))
                .route("ai-service", r -> r.path("/api/v1/ai/**")
                        .uri(aiServiceUrl))
                .build();
    }
}

