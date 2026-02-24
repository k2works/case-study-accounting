package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.AuthUseCase;
import com.example.accounting.application.port.in.RecordAuditLogUseCase;
import com.example.accounting.application.port.out.LoginResult;
import com.example.accounting.application.port.in.command.LoginCommand;
import com.example.accounting.domain.model.audit.AuditAction;
import com.example.accounting.domain.model.user.Role;
import com.example.accounting.infrastructure.web.dto.LoginRequest;
import com.example.accounting.infrastructure.web.dto.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
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

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 認証コントローラのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("認証コントローラ")
class AuthControllerTest {

    private static final String CLIENT_HOST = "client-host";

    @Mock
    private AuthUseCase authUseCase;

    @Mock
    private RecordAuditLogUseCase recordAuditLogUseCase;

    @Mock
    private HttpServletRequest httpServletRequest;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authUseCase, recordAuditLogUseCase);
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
            when(httpServletRequest.getRemoteAddr()).thenReturn(CLIENT_HOST);
            when(authUseCase.execute(any(LoginCommand.class))).thenReturn(result);

            // When
            ResponseEntity<LoginResponse> response = authController.login(request, httpServletRequest);

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
            when(httpServletRequest.getRemoteAddr()).thenReturn(CLIENT_HOST);
            when(authUseCase.execute(any(LoginCommand.class)))
                    .thenReturn(LoginResult.failure("error"));

            // When
            authController.login(request, httpServletRequest);

            // Then
            ArgumentCaptor<LoginCommand> captor = ArgumentCaptor.forClass(LoginCommand.class);
            verify(authUseCase).execute(captor.capture());
            assertThat(captor.getValue().username()).isEqualTo("testuser");
            assertThat(captor.getValue().password()).isEqualTo("Password123!");
        }

        @Test
        @DisplayName("不正な認証情報でログインに失敗する")
        void shouldFailWithInvalidCredentials() {
            // Given
            LoginRequest request = new LoginRequest("testuser", "wrongpassword");
            LoginResult result = LoginResult.failure("ユーザー名またはパスワードが正しくありません");
            when(httpServletRequest.getRemoteAddr()).thenReturn(CLIENT_HOST);
            when(authUseCase.execute(any(LoginCommand.class))).thenReturn(result);

            // When
            ResponseEntity<LoginResponse> response = authController.login(request, httpServletRequest);

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
            when(httpServletRequest.getRemoteAddr()).thenReturn(CLIENT_HOST);
            when(authUseCase.execute(any(LoginCommand.class))).thenReturn(result);

            // When
            ResponseEntity<LoginResponse> response = authController.login(request, httpServletRequest);

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
            LoginResult result = LoginResult.failure("アカウントが無効です");
            when(httpServletRequest.getRemoteAddr()).thenReturn(CLIENT_HOST);
            when(authUseCase.execute(any(LoginCommand.class))).thenReturn(result);

            // When
            ResponseEntity<LoginResponse> response = authController.login(request, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
            assertThat(response.getBody().errorMessage()).isEqualTo("アカウントが無効です");
        }

        @Test
        @DisplayName("ログイン成功時に監査ログを記録する")
        void shouldRecordAuditLogWhenLoginSucceeds() {
            LoginRequest request = new LoginRequest("testuser", "Password123!");
            when(httpServletRequest.getRemoteAddr()).thenReturn(CLIENT_HOST);
            when(authUseCase.execute(any(LoginCommand.class))).thenReturn(
                    LoginResult.success("access_token", "refresh_token", "testuser", Role.USER)
            );

            authController.login(request, httpServletRequest);

            ArgumentCaptor<RecordAuditLogUseCase.RecordAuditLogCommand> captor =
                    ArgumentCaptor.forClass(RecordAuditLogUseCase.RecordAuditLogCommand.class);
            verify(recordAuditLogUseCase).execute(captor.capture());
            assertThat(captor.getValue().userId()).isEqualTo("testuser");
            assertThat(captor.getValue().actionType()).isEqualTo(AuditAction.LOGIN);
            assertThat(captor.getValue().entityType()).isNull();
            assertThat(captor.getValue().entityId()).isNull();
            assertThat(captor.getValue().description()).isEqualTo("ログイン成功");
            assertThat(captor.getValue().ipAddress()).isEqualTo(CLIENT_HOST);
        }

        @Test
        @DisplayName("ログイン失敗時に監査ログを記録する")
        void shouldRecordAuditLogWhenLoginFails() {
            LoginRequest request = new LoginRequest("testuser", "wrongpassword");
            when(httpServletRequest.getRemoteAddr()).thenReturn(CLIENT_HOST);
            when(authUseCase.execute(any(LoginCommand.class))).thenReturn(LoginResult.failure("認証失敗"));

            authController.login(request, httpServletRequest);

            ArgumentCaptor<RecordAuditLogUseCase.RecordAuditLogCommand> captor =
                    ArgumentCaptor.forClass(RecordAuditLogUseCase.RecordAuditLogCommand.class);
            verify(recordAuditLogUseCase).execute(captor.capture());
            assertThat(captor.getValue().userId()).isEqualTo("testuser");
            assertThat(captor.getValue().actionType()).isEqualTo(AuditAction.LOGIN);
            assertThat(captor.getValue().entityType()).isNull();
            assertThat(captor.getValue().entityId()).isNull();
            assertThat(captor.getValue().description()).isEqualTo("ログイン失敗");
            assertThat(captor.getValue().ipAddress()).isEqualTo(CLIENT_HOST);
        }

        @Test
        @DisplayName("監査ログ記録で例外が発生してもログイン処理は成功する")
        void shouldLoginEvenWhenAuditLogFails() {
            LoginRequest request = new LoginRequest("testuser", "Password123!");
            when(httpServletRequest.getRemoteAddr()).thenReturn(CLIENT_HOST);
            when(authUseCase.execute(any(LoginCommand.class))).thenReturn(
                    LoginResult.success("access_token", "refresh_token", "testuser", Role.USER)
            );
            when(recordAuditLogUseCase.execute(any())).thenThrow(new RuntimeException("audit error"));

            ResponseEntity<LoginResponse> response = authController.login(request, httpServletRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("ログアウト")
    class Logout {

        @Test
        @DisplayName("ログアウト時に監査ログを記録して 200 を返す")
        void shouldRecordAuditLogAndReturnOk() {
            Principal principal = () -> "testuser";
            when(httpServletRequest.getRemoteAddr()).thenReturn(CLIENT_HOST);

            ResponseEntity<Void> response = authController.logout(principal, httpServletRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<RecordAuditLogUseCase.RecordAuditLogCommand> captor =
                    ArgumentCaptor.forClass(RecordAuditLogUseCase.RecordAuditLogCommand.class);
            verify(recordAuditLogUseCase).execute(captor.capture());
            assertThat(captor.getValue().userId()).isEqualTo("testuser");
            assertThat(captor.getValue().actionType()).isEqualTo(AuditAction.LOGOUT);
            assertThat(captor.getValue().entityType()).isNull();
            assertThat(captor.getValue().entityId()).isNull();
            assertThat(captor.getValue().description()).isEqualTo("ログアウト");
            assertThat(captor.getValue().ipAddress()).isEqualTo(CLIENT_HOST);
        }

        @Test
        @DisplayName("Principal が null の場合は監査ログを記録せずに 200 を返す")
        void shouldReturnOkWithoutAuditWhenPrincipalIsNull() {
            ResponseEntity<Void> response = authController.logout(null, httpServletRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verifyNoInteractions(recordAuditLogUseCase);
        }
    }
}
