package com.example.accounting.infrastructure.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("JwtAuthenticationFilter")
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("トークンがない場合")
    class WithoutToken {

        @Test
        @DisplayName("認証なしでフィルタチェーンを続行する")
        void shouldContinueWithoutAuthentication() throws Exception {
            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    @DisplayName("有効なトークンがある場合")
    class WithValidToken {

        @Test
        @DisplayName("認証情報を設定してフィルタチェーンを続行する")
        void shouldSetAuthenticationAndContinue() throws Exception {
            String token = "valid.jwt.token";
            request.addHeader("Authorization", "Bearer " + token);

            when(claims.getSubject()).thenReturn("testuser");
            when(claims.get("role", String.class)).thenReturn("USER");
            when(jwtService.isTokenValid(token)).thenReturn(true);
            when(jwtService.extractClaims(token)).thenReturn(Optional.of(claims));

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getName()).isEqualTo("testuser");
            assertThat(authentication.getAuthorities())
                    .anyMatch(a -> "ROLE_USER".equals(a.getAuthority()));
        }
    }

    @Nested
    @DisplayName("無効なトークンがある場合")
    class WithInvalidToken {

        @Test
        @DisplayName("認証なしでフィルタチェーンを続行する")
        void shouldContinueWithoutAuthentication() throws Exception {
            String token = "invalid.jwt.token";
            request.addHeader("Authorization", "Bearer " + token);

            when(jwtService.isTokenValid(token)).thenReturn(false);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    @DisplayName("Authorization ヘッダーの形式")
    class AuthorizationHeaderFormat {

        @Test
        @DisplayName("Bearer プレフィックスがない場合は認証しない")
        void shouldNotAuthenticateWithoutBearerPrefix() throws Exception {
            request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }
}
