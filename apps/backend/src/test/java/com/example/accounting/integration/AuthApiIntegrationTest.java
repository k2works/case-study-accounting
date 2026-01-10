package com.example.accounting.integration;

import com.example.accounting.TestcontainersConfiguration;
import com.example.accounting.infrastructure.web.dto.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 認証 API 統合テスト
 *
 * <p>Testcontainers を使用して実際の PostgreSQL データベースと連携し、
 * 認証フローの E2E テストを実行する。</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@DisplayName("認証 API 統合テスト")
class AuthApiIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient createRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    private LoginResponse performLogin(RestClient restClient, String requestBody) {
        return restClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(LoginResponse.class);
    }

    private String createLoginRequestBody(String username, String password) {
        return """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);
    }

    @Nested
    @DisplayName("ログイン API")
    class LoginApi {

        @ParameterizedTest(name = "{0} ユーザーがログインできる")
        @CsvSource({
                "admin, ADMIN, 管理者",
                "manager, MANAGER, 経理責任者",
                "user, USER, 経理担当者",
                "viewer, VIEWER, 閲覧者"
        })
        @DisplayName("正しい認証情報でログインできる")
        void shouldLoginWithValidCredentials(String username, String expectedRole, String displayName) {
            // Given
            RestClient restClient = createRestClient();
            String requestBody = createLoginRequestBody(username, "Password123!");

            // When
            LoginResponse response = performLogin(restClient, requestBody);

            // Then
            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.success()).isTrue();
                        assertThat(r.accessToken()).isNotNull().isNotEmpty();
                        assertThat(r.refreshToken()).isNotNull().isNotEmpty();
                        assertThat(r.username()).isEqualTo(username);
                        assertThat(r.role()).isEqualTo(expectedRole);
                        assertThat(r.errorMessage()).isNull();
                    });
        }

        @ParameterizedTest(name = "{2}")
        @CsvSource({
                "admin, WrongPassword123!, 不正なパスワードでログインに失敗する",
                "nonexistent, Password123!, 存在しないユーザーでログインに失敗する",
                "locked, Password123!, ロックされたアカウントでログインに失敗する"
        })
        @DisplayName("認証失敗時は 401 を返す")
        void shouldFailWithUnauthorized(String username, String password, String scenario) {
            // Given
            RestClient restClient = createRestClient();
            String requestBody = createLoginRequestBody(username, password);

            // When / Then
            assertThatThrownBy(() -> performLogin(restClient, requestBody))
                    .isInstanceOf(HttpClientErrorException.class)
                    .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(HttpClientErrorException.class))
                    .extracting(HttpClientErrorException::getStatusCode)
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @ParameterizedTest(name = "{2}")
        @CsvSource({
                "'', Password123!, 空のユーザー名でログインに失敗する",
                "admin, '', 空のパスワードでログインに失敗する"
        })
        @DisplayName("バリデーションエラー時は 400 を返す")
        void shouldFailWithBadRequest(String username, String password, String scenario) {
            // Given
            RestClient restClient = createRestClient();
            String requestBody = createLoginRequestBody(username, password);

            // When / Then
            assertThatThrownBy(() -> performLogin(restClient, requestBody))
                    .isInstanceOf(HttpClientErrorException.class)
                    .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(HttpClientErrorException.class))
                    .extracting(HttpClientErrorException::getStatusCode)
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("JWT トークン検証")
    class JwtTokenValidation {

        @Test
        @DisplayName("ログイン後に取得した JWT トークンが正しい形式である")
        void shouldReturnValidJwtTokenFormat() {
            // Given
            RestClient restClient = createRestClient();
            String requestBody = createLoginRequestBody("admin", "Password123!");

            // When
            LoginResponse response = performLogin(restClient, requestBody);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isNotNull();

            // JWT 形式の検証（header.payload.signature）
            String[] parts = response.accessToken().split("\\.");
            assertThat(parts).hasSize(3);

            // 各パートが空でないことを確認
            assertThat(parts[0]).isNotEmpty(); // header
            assertThat(parts[1]).isNotEmpty(); // payload
            assertThat(parts[2]).isNotEmpty(); // signature
        }

        @Test
        @DisplayName("アクセストークンとリフレッシュトークンが異なる")
        void shouldReturnDifferentTokens() {
            // Given
            RestClient restClient = createRestClient();
            String requestBody = createLoginRequestBody("admin", "Password123!");

            // When
            LoginResponse response = performLogin(restClient, requestBody);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isNotEqualTo(response.refreshToken());
        }
    }
}
