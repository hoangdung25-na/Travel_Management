package com.travel.gateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.core.dto.ApiResponse;
import com.travel.common.core.exception.ErrorCode;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class GatewayAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String path = exchange.getRequest().getURI().getPath();
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        String errorMessage = "Yêu cầu xác thực tài khoản";

        // Distinguish AUTH-1002 (Token Expired) vs AUTH-1001 (Token Invalid)
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        String messageLower = cause.getMessage() != null ? cause.getMessage().toLowerCase() : "";

        if (messageLower.contains("expired") || messageLower.contains("hết hạn")) {
            errorCode = ErrorCode.AUTH_TOKEN_EXPIRED;
            errorMessage = "Token đã hết hạn";
        } else if (messageLower.contains("invalid") || messageLower.contains("jwt") || messageLower.contains("bearer")) {
            errorCode = ErrorCode.AUTH_TOKEN_INVALID;
            errorMessage = "Token không hợp lệ";
        }

        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), errorMessage, path);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsString(apiResponse).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            bytes = "{\"success\":false,\"code\":\"ERR-0401\",\"message\":\"Unauthorized\"}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
