package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.TestcontainersConfiguration;
import com.example.accounting.infrastructure.web.dto.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient createRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Nested
    @DisplayName("ログイン API")
    class LoginApi {

        @Test
        @DisplayName("正しい認証情報で管理者ユーザーがログインできる")
        void shouldLoginWithAdminCredentials() {
            // Given
            RestClient restClient = createRestClient();
            String requestBody = """
                    {
                        "username": "admin",
                        "password": "Password123!"
                    }
                    """;

            // When
            LoginResponse response = restClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(LoginResponse.class);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.success()).isTrue();
            assertThat(response.accessToken()).isNotNull().isNotEmpty();
            assertThat(response.refreshToken()).isNotNull().isNotEmpty();
            assertThat(response.username()).isEqualTo("admin");
            assertThat(response.role()).isEqualTo("ADMIN");
            assertThat(response.errorMessage()).isNull();
        }

        @Test
        @DisplayName("正しい認証情報で経理責任者ユーザーがログインできる")
        void shouldLoginWithManagerCredentials() {
            // Given
            RestClient restClient = createRestClient();
            String requestBody = """
                    {
                        "username": "manager",
                        "password": "Password123!"
                    }
                    """;

            // When
            LoginResponse response = restClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(LoginResponse.class);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.success()).isTrue();
            assertThat(response.username()).isEqualTo("manager");
            assertThat(response.role()).isEqualTo("MANAGER");
        }

        @Test
        @DisplayName("正しい認証情報で経理担当者ユーザーがログインできる")
        void shouldLoginWithUserCredentials() {
            // Given
            RestClient restClient = createRestClient();
            String requestBody = """
                    {
                        "username": "user",
                        "password": "Password123!"
                    }
                    """;

            // When
            LoginResponse response = restClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(LoginResponse.class);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.success()).isTrue();
            assertThat(response.username()).isEqualTo("user");
            assertThat(response.role()).isEqualTo("USER");
        }

        @Test
        @DisplayName("正しい認証情報で閲覧者ユーザーがログインできる")
        void shouldLoginWithViewerCredentials() {
            // Given
            RestClient restClient = createRestClient();
            String requestBody = """
                    {
                        "username": "viewer",
                        "password": "Password123!"
                    }
                    """;

            // When
            LoginResponse response = restClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(LoginResponse.class);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.success()).isTrue();
            assertThat(response.username()).isEqualTo("viewer");
            assertThat(response.role()).isEqualTo("VIEWER");
        }

        @Test
        @DisplayName("不正なパスワードでログインに失敗する")
        void shouldFailWithWrongPassword() {
            // Given
            RestClient restClient = createRestClient();
            String requestBody = """
                    {
                        "username": "admin",
                        "password": "WrongPassword123!"
                    }
                    """;

            // When / Then
            assertThatThrownBy(() -> restClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(LoginResponse.class))
                    .isInstanceOf(HttpClientErrorException.class)
                    .satisfies(ex -> {
                        HttpClientErrorException httpEx = (HttpClientErrorException) ex;
                        assertThat(httpEx.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }

        @Test
        @DisplayName("存在しないユーザーでログインに失敗する")
        void shouldFailWithNonExistentUser() {
            // Given
            RestClient restClient = createRestClient();
            String requestBody = """
                    {
                        "username": "nonexistent",
                        "password": "Password123!"
                    }
                    """;

            // When / Then
            assertThatThrownBy(() -> restClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(LoginResponse.class))
                    .isInstanceOf(HttpClientErrorException.class)
                    .satisfies(ex -> {
                        HttpClientErrorException httpEx = (HttpClientErrorException) ex;
                        assertThat(httpEx.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }

        @Test
        @DisplayName("ロックされたアカウントでログインに失敗する")
        void shouldFailWithLockedAccount() {
            // Given
            RestClient restClient = createRestClient();
            String requestBody = """
                    {
                        "username": "locked",
                        "password": "Password123!"
                    }
                    """;

            // When / Then
            assertThatThrownBy(() -> restClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(LoginResponse.class))
                    .isInstanceOf(HttpClientErrorException.class)
                    .satisfies(ex -> {
                        HttpClientErrorException httpEx = (HttpClientErrorException) ex;
                        assertThat(httpEx.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    });
        }

        @Test
        @DisplayName("空のユーザー名でログインに失敗する")
        void shouldFailWithEmptyUsername() {
            // Given
            RestClient restClient = createRestClient();
            String requestBody = """
                    {
                        "username": "",
                        "password": "Password123!"
                    }
                    """;

            // When / Then
            assertThatThrownBy(() -> restClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(LoginResponse.class))
                    .isInstanceOf(HttpClientErrorException.class)
                    .satisfies(ex -> {
                        HttpClientErrorException httpEx = (HttpClientErrorException) ex;
                        assertThat(httpEx.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }

        @Test
        @DisplayName("空のパスワードでログインに失敗する")
        void shouldFailWithEmptyPassword() {
            // Given
            RestClient restClient = createRestClient();
            String requestBody = """
                    {
                        "username": "admin",
                        "password": ""
                    }
                    """;

            // When / Then
            assertThatThrownBy(() -> restClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(LoginResponse.class))
                    .isInstanceOf(HttpClientErrorException.class)
                    .satisfies(ex -> {
                        HttpClientErrorException httpEx = (HttpClientErrorException) ex;
                        assertThat(httpEx.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
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
            String requestBody = """
                    {
                        "username": "admin",
                        "password": "Password123!"
                    }
                    """;

            // When
            LoginResponse response = restClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(LoginResponse.class);

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
            String requestBody = """
                    {
                        "username": "admin",
                        "password": "Password123!"
                    }
                    """;

            // When
            LoginResponse response = restClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(LoginResponse.class);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isNotEqualTo(response.refreshToken());
        }
    }
}
