package com.easytickets.application.config;

import com.easytickets.common.constant.AppConstants;
import com.easytickets.common.dto.ApiResponse;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Ensures 401/403 responses from the security filter chain (missing/invalid JWT,
 * missing role for @PreAuthorize) follow the project-wide ApiResponse contract
 * instead of the servlet container's default error page.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JsonSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException {
        log.warn("Unauthenticated request rejected. path={}", request.getRequestURI());
        writeError(response, HttpStatus.UNAUTHORIZED, AppConstants.INVALID_TOKEN, "Authentication required");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) throws IOException {
        log.warn("Access denied. path={}", request.getRequestURI());
        writeError(response, HttpStatus.FORBIDDEN, AppConstants.FORBIDDEN, "Access denied");
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String errorCode, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(errorCode, message)));
    }
}
