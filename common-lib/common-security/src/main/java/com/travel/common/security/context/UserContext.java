package com.travel.common.security.context;

public final class UserContext {
    private static final ThreadLocal<String> CURRENT_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USER_ROLES = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USER_EMAIL = new ThreadLocal<>();

    private UserContext() {
    }

    public static String getUserId() {
        return CURRENT_USER_ID.get();
    }

    public static void setUserId(String userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static String getUserRoles() {
        return CURRENT_USER_ROLES.get();
    }

    public static void setUserRoles(String userRoles) {
        CURRENT_USER_ROLES.set(userRoles);
    }

    public static String getUserEmail() {
        return CURRENT_USER_EMAIL.get();
    }

    public static void setUserEmail(String userEmail) {
        CURRENT_USER_EMAIL.set(userEmail);
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
        CURRENT_USER_ROLES.remove();
        CURRENT_USER_EMAIL.remove();
    }
}
