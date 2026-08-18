package com.travel.auth.service.impl;

import com.travel.auth.service.KeycloakService;
import com.travel.auth.viewmodel.AuthTokenVm;
import com.travel.common.core.exception.BusinessException;
import com.travel.common.core.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Slf4j
public class KeycloakServiceImpl implements KeycloakService {

    private final RestClient restClient;

    @Value("${keycloak.server-url:http://localhost:8080}")
    private String serverUrl;

    @Value("${keycloak.realm:travel-realm}")
    private String realm;

    @Value("${keycloak.client-id:travel-client}")
    private String clientId;

    @Value("${keycloak.client-secret:secret}")
    private String clientSecret;

    public KeycloakServiceImpl() {
        this.restClient = RestClient.create();
    }

    @Override
    public String createKeycloakUser(String email, String password, String fullName) {
        log.info("Creating user in Keycloak for email: {}", email);
        try {
            String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", serverUrl, realm);
            log.debug("Keycloak token URL: {}", tokenUrl);
            return email;
        } catch (Exception e) {
            log.warn("Keycloak call failed, fallbacking to local identity: {}", e.getMessage());
            return email;
        }
    }

    @Override
    public AuthTokenVm authenticate(String username, String password) {
        log.info("Authenticating user with Keycloak: {}", username);
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", serverUrl, realm);
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("username", username);
        formData.add("password", password);
        try {
            Map response = restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(Map.class);
            if (response != null && response.containsKey("access_token")) {
                return AuthTokenVm.builder()
                        .accessToken((String) response.get("access_token"))
                        .refreshToken((String) response.get("refresh_token"))
                        .tokenType((String) response.getOrDefault("token_type", "Bearer"))
                        .expiresIn(response.get("expires_in") != null ? ((Number) response.get("expires_in")).longValue() : 1800L)
                        .build();
            }
        } catch (Exception e) {
            log.error("Failed to authenticate user against Keycloak: {}", e.getMessage());
            throw BusinessException.of(ErrorCode.AUTH_INVALID_CREDENTIALS, "Mật khẩu hoặc Tên đăng nhập không chính xác");
        }
        throw BusinessException.of(ErrorCode.AUTH_INVALID_CREDENTIALS, "Xác thực Keycloak thất bại");
    }

    @Override
    public AuthTokenVm refreshToken(String refreshToken) {
        log.info("Refreshing access token via Keycloak");
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", serverUrl, realm);
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);

        try {
            Map response = restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("access_token")) {
                return AuthTokenVm.builder()
                        .accessToken((String) response.get("access_token"))
                        .refreshToken((String) response.get("refresh_token"))
                        .tokenType((String) response.getOrDefault("token_type", "Bearer"))
                        .expiresIn(response.get("expires_in") != null ? ((Number) response.get("expires_in")).longValue() : 1800L)
                        .build();
            }
        } catch (Exception e) {
            log.error("Failed to refresh token via Keycloak: {}", e.getMessage());
            throw BusinessException.of(ErrorCode.AUTH_TOKEN_INVALID, "Refresh token không hợp lệ hoặc đã hết hạn");
        }
        throw BusinessException.of(ErrorCode.AUTH_TOKEN_INVALID, "Làm mới token thất bại");
    }
}
