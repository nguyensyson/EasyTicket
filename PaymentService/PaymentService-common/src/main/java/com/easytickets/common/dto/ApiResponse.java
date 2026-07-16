package com.easytickets.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String errorCode;    // null when success
    private String message;
    private T data;              // null when error
    private String traceId;      // injected from MDC

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .traceId(currentTraceId())
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .traceId(currentTraceId())
                .build();
    }

    public static ApiResponse<Void> error(String errorCode, String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .traceId(currentTraceId())
                .build();
    }

    private static String currentTraceId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : "";
    }
}
