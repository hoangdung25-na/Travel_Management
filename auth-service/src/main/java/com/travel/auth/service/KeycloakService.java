package com.travel.auth.service;

import com.travel.auth.viewmodel.AuthTokenVm;

public interface KeycloakService {
    String createKeycloakUser(String email, String password, String fullName);
    AuthTokenVm authenticate(String username, String password);
    AuthTokenVm refreshToken(String refreshToken);
}
