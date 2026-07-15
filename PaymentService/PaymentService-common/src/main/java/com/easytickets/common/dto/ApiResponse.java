package com.easytickets.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    public static ApiResponse<Void> error(String errorCode, String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .build();
    }
}
