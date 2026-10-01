package com.travel.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GlobalHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder builder = request.mutate();

        String userId = request.getHeaders().getFirst("X-User-Id");
        String userRoles = request.getHeaders().getFirst("X-User-Roles");
        String userEmail = request.getHeaders().getFirst("X-User-Email");

        if (userId != null && !userId.isBlank()) {
            builder.header("X-User-Id", userId);
        }
        if (userRoles != null && !userRoles.isBlank()) {
            builder.header("X-User-Roles", userRoles);
        }
        if (userEmail != null && !userEmail.isBlank()) {
            builder.header("X-User-Email", userEmail);
        }

        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
