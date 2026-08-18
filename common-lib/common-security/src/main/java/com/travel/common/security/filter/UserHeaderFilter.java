package com.travel.common.security.filter;

import com.travel.common.security.context.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UserHeaderFilter extends OncePerRequestFilter {
    public static final String X_USER_ID = "X-User-Id";
    public static final String X_USER_ROLES = "X-User-Roles";
    public static final String X_USER_EMAIL = "X-User-Email";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String userId = request.getHeader(X_USER_ID);
            String userRoles = request.getHeader(X_USER_ROLES);
            String userEmail = request.getHeader(X_USER_EMAIL);

            if (userId != null && !userId.isBlank()) {
                UserContext.setUserId(userId);

                List<GrantedAuthority> authorities = Collections.emptyList();
                if (userRoles != null && !userRoles.isBlank()) {
                    UserContext.setUserRoles(userRoles);
                    authorities = Arrays.stream(userRoles.split(","))
                            .map(String::trim)
                            .filter(role -> !role.isEmpty())
                            .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                            .map(SimpleGrantedAuthority::new)
                            .map(GrantedAuthority.class::cast)
                            .toList();
                }

                if (userEmail != null && !userEmail.isBlank()) {
                    UserContext.setUserEmail(userEmail);
                }

                PreAuthenticatedAuthenticationToken authentication =
                        new PreAuthenticatedAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}
