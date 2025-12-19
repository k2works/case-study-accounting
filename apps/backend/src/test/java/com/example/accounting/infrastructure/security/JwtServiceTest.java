package com.example.accounting.infrastructure.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService")
class JwtServiceTest {

    private static final String SECRET = "this-is-a-very-long-secret-key-for-testing-purposes-only-256-bits";
    private static final long EXPIRATION = 3600000L; // 1 hour
    private static final long REFRESH_EXPIRATION = 86400000L; // 24 hours

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(SECRET, EXPIRATION, REFRESH_EXPIRATION);
        jwtService = new JwtService(properties);
    }

    @Nested
    @DisplayName("トークン生成")
    class GenerateToken {

        @Test
        @DisplayName("subjectを含むトークンを生成できる")
        void shouldGenerateTokenWithSubject() {
            String subject = "user@example.com";

            String token = jwtService.generateToken(subject);

            assertThat(token).isNotBlank();
            assertThat(jwtService.extractSubject(token)).contains(subject);
        }

        @Test
        @DisplayName("カスタムクレームを含むトークンを生成できる")
        void shouldGenerateTokenWithCustomClaims() {
            String subject = "user@example.com";
            Map<String, Object> claims = Map.of("role", "ADMIN", "userId", 123);

            String token = jwtService.generateToken(subject, claims);

            Optional<Claims> extractedClaims = jwtService.extractClaims(token);
            assertThat(extractedClaims).isPresent();
            assertThat(extractedClaims.get().get("role")).isEqualTo("ADMIN");
            assertThat(extractedClaims.get().get("userId")).isEqualTo(123);
        }

        @Test
        @DisplayName("リフレッシュトークンを生成できる")
        void shouldGenerateRefreshToken() {
            String subject = "user@example.com";

            String refreshToken = jwtService.generateRefreshToken(subject);

            assertThat(refreshToken).isNotBlank();
            assertThat(jwtService.extractSubject(refreshToken)).contains(subject);
        }
    }

    @Nested
    @DisplayName("トークン検証")
    class ValidateToken {

        @Test
        @DisplayName("有効なトークンはtrueを返す")
        void shouldReturnTrueForValidToken() {
            String subject = "user@example.com";
            String token = jwtService.generateToken(subject);

            assertThat(jwtService.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("有効なトークンと正しいsubjectでtrueを返す")
        void shouldReturnTrueForValidTokenWithCorrectSubject() {
            String subject = "user@example.com";
            String token = jwtService.generateToken(subject);

            assertThat(jwtService.isTokenValid(token, subject)).isTrue();
        }

        @Test
        @DisplayName("有効なトークンと異なるsubjectでfalseを返す")
        void shouldReturnFalseForValidTokenWithWrongSubject() {
            String subject = "user@example.com";
            String token = jwtService.generateToken(subject);

            assertThat(jwtService.isTokenValid(token, "other@example.com")).isFalse();
        }

        @Test
        @DisplayName("不正なトークンはfalseを返す")
        void shouldReturnFalseForInvalidToken() {
            assertThat(jwtService.isTokenValid("invalid-token")).isFalse();
        }

        @Test
        @DisplayName("空のトークンはfalseを返す")
        void shouldReturnFalseForEmptyToken() {
            assertThat(jwtService.isTokenValid("")).isFalse();
        }
    }

    @Nested
    @DisplayName("クレーム抽出")
    class ExtractClaims {

        @Test
        @DisplayName("有効なトークンからsubjectを抽出できる")
        void shouldExtractSubjectFromValidToken() {
            String subject = "user@example.com";
            String token = jwtService.generateToken(subject);

            Optional<String> extractedSubject = jwtService.extractSubject(token);

            assertThat(extractedSubject).contains(subject);
        }

        @Test
        @DisplayName("不正なトークンからはOptional.emptyを返す")
        void shouldReturnEmptyForInvalidToken() {
            Optional<String> subject = jwtService.extractSubject("invalid-token");

            assertThat(subject).isEmpty();
        }
    }
}
