package com.travel.common.core.dto;

import com.travel.common.core.constant.MdcKey;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.List;

public record ApiResponse<T>(
    boolean success,
    String code,
    String message,
    T data,
    List<String> errors,
    String path,
    String traceId,
    Instant timestamp
) {
    public static final String SUCCESS_CODE = "OK";

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, SUCCESS_CODE, null, data, null, null, currentTraceId(), Instant.now());
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, SUCCESS_CODE, message, data, null, null, currentTraceId(), Instant.now());
    }

    public static ApiResponse<Void> error(String code, String message, String path) {
        return new ApiResponse<>(false, code, message, null, null, path, currentTraceId(), Instant.now());
    }

    public static ApiResponse<Void> error(String code, String message, List<String> errors, String path) {
        return new ApiResponse<>(false, code, message, null, errors, path, currentTraceId(), Instant.now());
    }

    private static String currentTraceId() {
        return MDC.get(MdcKey.TRACE_ID);
    }
}
