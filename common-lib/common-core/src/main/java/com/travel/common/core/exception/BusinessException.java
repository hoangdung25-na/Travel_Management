package com.travel.common.core.exception;

import lombok.Getter;

/**
 * Single business-level exception duy nhất đại diện cho mọi lỗi nghiệp vụ trong hệ thống Microservices.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final int status;

    public BusinessException(ErrorCode code) {
        super(code.getMessageKey());
        this.errorCode = code;
        this.status = code.getStatus();
    }

    public BusinessException(ErrorCode code, String message) {
        super(message);
        this.errorCode = code;
        this.status = code.getStatus();
    }

    public BusinessException(ErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = code;
        this.status = code.getStatus();
    }

    // ---- Static Factory Methods ----

    public static BusinessException of(ErrorCode code) {
        return new BusinessException(code);
    }

    public static BusinessException of(ErrorCode code, String message) {
        return new BusinessException(code, message);
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(ErrorCode.BAD_REQUEST, message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(ErrorCode.NOT_FOUND, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(ErrorCode.CONFLICT, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(ErrorCode.UNAUTHORIZED, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(ErrorCode.FORBIDDEN, message);
    }

    public static BusinessException internalServerError(String message) {
        return new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}
