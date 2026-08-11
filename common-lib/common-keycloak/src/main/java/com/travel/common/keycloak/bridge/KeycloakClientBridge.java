package com.travel.common.keycloak.bridge;

public interface KeycloakClientBridge {
    String authenticateUser(String username, String password);
    String registerUser(String username, String email, String password);
}
