package com.example.accounting.presentation.api.auth;

import com.example.accounting.application.usecase.auth.LoginCommand;
import com.example.accounting.application.usecase.auth.LoginResult;
import com.example.accounting.application.usecase.auth.LoginUseCase;
import com.example.accounting.domain.model.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 認証コントローラのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("認証コントローラ")
class AuthControllerTest {

    @Mock
    private LoginUseCase loginUseCase;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(loginUseCase);
    }

    @Nested
    @DisplayName("ログイン")
    class Login {

        @Test
        @DisplayName("正しい認証情報でログインできる")
        void shouldLoginWithCorrectCredentials() {
            // Given
            LoginRequest request = new LoginRequest("testuser", "Password123!");
            LoginResult result = LoginResult.success(
                    "access_token",
                    "refresh_token",
                    "testuser",
                    Role.USER
            );
            when(loginUseCase.execute(any(LoginCommand.class))).thenReturn(result);

            // When
            ResponseEntity<LoginResponse> response = authController.login(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isTrue();
            assertThat(response.getBody().accessToken()).isEqualTo("access_token");
            assertThat(response.getBody().refreshToken()).isEqualTo("refresh_token");
            assertThat(response.getBody().username()).isEqualTo("testuser");
            assertThat(response.getBody().role()).isEqualTo("USER");
            assertThat(response.getBody().errorMessage()).isNull();
        }

        @Test
        @DisplayName("リクエストの内容がLoginCommandに正しく変換される")
        void shouldConvertRequestToCommand() {
            // Given
            LoginRequest request = new LoginRequest("testuser", "Password123!");
            when(loginUseCase.execute(any(LoginCommand.class)))
                    .thenReturn(LoginResult.failure("error"));

            // When
            authController.login(request);

            // Then
            ArgumentCaptor<LoginCommand> captor = ArgumentCaptor.forClass(LoginCommand.class);
            verify(loginUseCase).execute(captor.capture());
            assertThat(captor.getValue().username()).isEqualTo("testuser");
            assertThat(captor.getValue().password()).isEqualTo("Password123!");
        }

        @Test
        @DisplayName("不正な認証情報でログインに失敗する")
        void shouldFailWithInvalidCredentials() {
            // Given
            LoginRequest request = new LoginRequest("testuser", "wrongpassword");
            LoginResult result = LoginResult.failure("ユーザー名またはパスワードが正しくありません");
            when(loginUseCase.execute(any(LoginCommand.class))).thenReturn(result);

            // When
            ResponseEntity<LoginResponse> response = authController.login(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
            assertThat(response.getBody().accessToken()).isNull();
            assertThat(response.getBody().errorMessage()).isEqualTo("ユーザー名またはパスワードが正しくありません");
        }

        @Test
        @DisplayName("ロックされたアカウントでログインに失敗する")
        void shouldFailWithLockedAccount() {
            // Given
            LoginRequest request = new LoginRequest("lockeduser", "Password123!");
            LoginResult result = LoginResult.failure("アカウントがロックされています");
            when(loginUseCase.execute(any(LoginCommand.class))).thenReturn(result);

            // When
            ResponseEntity<LoginResponse> response = authController.login(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
            assertThat(response.getBody().errorMessage()).isEqualTo("アカウントがロックされています");
        }

        @Test
        @DisplayName("無効化されたアカウントでログインに失敗する")
        void shouldFailWithDeactivatedAccount() {
            // Given
            LoginRequest request = new LoginRequest("deactivateduser", "Password123!");
            LoginResult result = LoginResult.failure("アカウントが無効化されています");
            when(loginUseCase.execute(any(LoginCommand.class))).thenReturn(result);

            // When
            ResponseEntity<LoginResponse> response = authController.login(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
            assertThat(response.getBody().errorMessage()).isEqualTo("アカウントが無効化されています");
        }
    }
}
