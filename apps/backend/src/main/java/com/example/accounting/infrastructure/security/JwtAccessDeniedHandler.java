package com.example.accounting.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * JWT アクセス拒否ハンドラ
 *
 * 認証済みだが権限不足の場合のレスポンスを定義
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    record ErrorResponse(String timestamp, int status, String error, String message, String path) {}

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        var errorResponse = new ErrorResponse(
                Instant.now().toString(), 403, "Forbidden", "権限がありません", request.getRequestURI());

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
