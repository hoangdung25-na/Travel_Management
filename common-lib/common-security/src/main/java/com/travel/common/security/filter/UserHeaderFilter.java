package com.travel.common.security.filter;

import com.travel.common.security.context.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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

            if (userId != null) {
                UserContext.setUserId(userId);
            }
            if (userRoles != null) {
                UserContext.setUserRoles(userRoles);
            }
            if (userEmail != null) {
                UserContext.setUserEmail(userEmail);
            }

            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
