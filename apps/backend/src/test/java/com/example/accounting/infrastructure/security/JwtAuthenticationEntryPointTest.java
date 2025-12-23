package com.example.accounting.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtAuthenticationEntryPoint")
class JwtAuthenticationEntryPointTest {

    private JwtAuthenticationEntryPoint entryPoint;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        entryPoint = new JwtAuthenticationEntryPoint();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("認証エラー時に 401 レスポンスを返す")
    void shouldReturn401OnAuthenticationError() throws Exception {
        request.setRequestURI("/api/protected");

        entryPoint.commence(request, response, new BadCredentialsException("Bad credentials"));

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
    }

    @Test
    @DisplayName("エラーレスポンスに必要な情報が含まれる")
    @SuppressWarnings("unchecked")
    void shouldContainErrorDetails() throws Exception {
        request.setRequestURI("/api/users");

        entryPoint.commence(request, response, new BadCredentialsException("Test error"));

        Map<String, Object> errorDetails = objectMapper.readValue(
                response.getContentAsString(), Map.class);

        assertThat(errorDetails).containsKey("timestamp");
        assertThat(errorDetails.get("status")).isEqualTo(401);
        assertThat(errorDetails.get("error")).isEqualTo("Unauthorized");
        assertThat(errorDetails.get("message")).isEqualTo("認証が必要です");
        assertThat(errorDetails.get("path")).isEqualTo("/api/users");
    }
}
