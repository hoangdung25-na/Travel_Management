package com.travel.common.core.exception;

import lombok.Getter;

/**
 * Centralized error catalog cho toàn bộ hệ thống Travel Microservices Platform.
 */
@Getter
public enum ErrorCode {

    // ---- Generic Errors (ERR-0xxx) ----
    BAD_REQUEST("ERR-0400", "error.bad.request", 400),
    UNAUTHORIZED("ERR-0401", "error.unauthorized", 401),
    FORBIDDEN("ERR-0403", "error.forbidden", 403),
    NOT_FOUND("ERR-0404", "error.not.found", 404),
    METHOD_NOT_ALLOWED("ERR-0405", "error.method.not.allowed", 405),
    CONFLICT("ERR-0409", "error.conflict", 409),
    VALIDATION_FAILED("ERR-0422-V", "error.validation.failed", 400),
    INTERNAL_SERVER_ERROR("ERR-0500", "error.internal.server", 500),

    // ---- Auth Domain (AUTH-1xxx) ----
    AUTH_TOKEN_INVALID("AUTH-1001", "auth.token.invalid", 401),
    AUTH_TOKEN_EXPIRED("AUTH-1002", "auth.token.expired", 401),
    AUTH_EMAIL_EXISTS("AUTH-1003", "auth.email.exists", 409),
    AUTH_USER_NOT_FOUND("AUTH-1004", "auth.user.not.found", 404),
    AUTH_ROLE_NOT_FOUND("AUTH-1005", "auth.role.not.found", 404),
    AUTH_INVALID_CREDENTIALS("AUTH-1006", "auth.invalid.credentials", 401),

    // ---- Tour Domain (TOUR-2xxx) ----
    TOUR_NOT_FOUND("TOUR-2001", "tour.not.found", 404),
    TOUR_CODE_EXISTS("TOUR-2002", "tour.code.exists", 409),
    TOUR_SCHEDULE_NOT_FOUND("TOUR-2003", "tour.schedule.not.found", 404),
    TOUR_NOT_PUBLISHED("TOUR-2004", "tour.not.published", 400),
    TOUR_SEATS_INSUFFICIENT("TOUR-2005", "tour.seats.insufficient", 409),

    // ---- Booking Domain (BKG-3xxx) ----
    BOOKING_NOT_FOUND("BKG-3001", "booking.not.found", 404),
    BOOKING_EXPIRED("BKG-3002", "booking.expired", 400),
    BOOKING_CANCELLED("BKG-3003", "booking.cancelled", 400),

    // ---- Payment Domain (PAY-4xxx) ----
    PAYMENT_FAILED("PAY-4001", "payment.failed", 400),
    PAYMENT_NOT_FOUND("PAY-4002", "payment.not.found", 404),

    // ---- AI Domain (AI-5xxx) ----
    AI_SERVICE_UNAVAILABLE("AI-5001", "ai.service.unavailable", 503);

    private final String code;
    private final String messageKey;
    private final int status;

    ErrorCode(String code, String messageKey, int status) {
        this.code = code;
        this.messageKey = messageKey;
        this.status = status;
    }
}
