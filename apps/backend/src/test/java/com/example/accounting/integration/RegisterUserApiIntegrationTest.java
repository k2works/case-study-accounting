package com.example.accounting.integration;

import com.example.accounting.TestcontainersConfiguration;
import com.example.accounting.infrastructure.web.dto.LoginResponse;
import com.example.accounting.infrastructure.web.dto.RegisterUserResponse;
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
 * ユーザー登録 API 統合テスト
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@DisplayName("ユーザー登録 API 統合テスト")
class RegisterUserApiIntegrationTest {

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

    private RegisterUserResponse performRegister(RestClient restClient, String token, String requestBody) {
        return restClient.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(requestBody)
                .retrieve()
                .body(RegisterUserResponse.class);
    }

    private String createLoginRequestBody(String username, String password) {
        return """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);
    }

    private String createRegisterRequestBody(
            String username,
            String email,
            String password,
            String displayName,
            String role
    ) {
        return """
                {
                    "username": "%s",
                    "email": "%s",
                    "password": "%s",
                    "displayName": "%s",
                    "role": "%s"
                }
                """.formatted(username, email, password, displayName, role);
    }

    private String loginAndGetToken(RestClient restClient, String username, String password) {
        LoginResponse response = performLogin(restClient, createLoginRequestBody(username, password));
        return response.accessToken();
    }

    @Nested
    @DisplayName("ユーザー登録 API")
    class RegisterApi {

        @Test
        @DisplayName("管理者はユーザー登録できる")
        void shouldRegisterUserAsAdmin() {
            // Given
            RestClient restClient = createRestClient();
            String token = loginAndGetToken(restClient, "admin", "Password123!");
            String requestBody = createRegisterRequestBody(
                    "newuser",
                    "newuser@example.com",
                    "Password123!",
                    "新規ユーザー",
                    "USER"
            );

            // When
            RegisterUserResponse response = performRegister(restClient, token, requestBody);

            // Then
            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.success()).isTrue();
                        assertThat(r.username()).isEqualTo("newuser");
                        assertThat(r.email()).isEqualTo("newuser@example.com");
                        assertThat(r.displayName()).isEqualTo("新規ユーザー");
                        assertThat(r.role()).isEqualTo("USER");
                        assertThat(r.errorMessage()).isNull();
                    });
        }

        @Test
        @DisplayName("管理者以外はユーザー登録できない")
        void shouldRejectRegisterForNonAdmin() {
            // Given
            RestClient restClient = createRestClient();
            String token = loginAndGetToken(restClient, "user", "Password123!");
            String requestBody = createRegisterRequestBody(
                    "otheruser",
                    "otheruser@example.com",
                    "Password123!",
                    "別ユーザー",
                    "USER"
            );

            // When / Then
            assertThatThrownBy(() -> performRegister(restClient, token, requestBody))
                    .isInstanceOf(HttpClientErrorException.class)
                    .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(HttpClientErrorException.class))
                    .extracting(HttpClientErrorException::getStatusCode)
                    .isEqualTo(HttpStatus.FORBIDDEN);
        }
    }
}
