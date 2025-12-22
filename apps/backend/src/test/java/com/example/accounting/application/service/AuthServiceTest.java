package com.example.accounting.application.service;

import com.example.accounting.application.port.in.LoginResult;
import com.example.accounting.application.port.in.command.LoginCommand;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.user.Role;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 認証サービスのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("認証サービス")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, jwtService);
    }

    @Nested
    @DisplayName("ログイン成功")
    class SuccessfulLogin {

        @Test
        @DisplayName("正しい認証情報でログインできる")
        void shouldLoginWithCorrectCredentials() {
            // Given
            String username = "testuser";
            String password = "Password123!";
            User user = User.create(username, "test@example.com", password, "テストユーザー", Role.USER);

            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
            when(jwtService.generateToken(anyString(), anyMap())).thenReturn("access_token");
            when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh_token");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            LoginCommand command = new LoginCommand(username, password);

            // When
            LoginResult result = authService.execute(command);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.accessToken()).isEqualTo("access_token");
            assertThat(result.refreshToken()).isEqualTo("refresh_token");
            assertThat(result.username()).isEqualTo(username);
            assertThat(result.role()).isEqualTo(Role.USER);
            assertThat(result.errorMessage()).isNull();

            // ログイン成功時に更新されたユーザーが保存される
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getLastLoginAt()).isNotNull();
            assertThat(savedUser.getFailedLoginAttempts()).isZero();
        }
    }

    @Nested
    @DisplayName("ログイン失敗")
    class FailedLogin {

        @Test
        @DisplayName("存在しないユーザー名でログインに失敗する")
        void shouldFailWithNonExistentUsername() {
            // Given
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            LoginCommand command = new LoginCommand("nonexistent", "password");

            // When
            LoginResult result = authService.execute(command);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.accessToken()).isNull();
            assertThat(result.errorMessage()).contains("ユーザー名またはパスワードが正しくありません");

            verify(jwtService, never()).generateToken(anyString(), anyMap());
        }

        @Test
        @DisplayName("間違ったパスワードでログインに失敗する")
        void shouldFailWithWrongPassword() {
            // Given
            String username = "testuser";
            User user = User.create(username, "test@example.com", "CorrectPassword123!", "テストユーザー", Role.USER);

            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            LoginCommand command = new LoginCommand(username, "WrongPassword");

            // When
            LoginResult result = authService.execute(command);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).contains("ユーザー名またはパスワードが正しくありません");

            // 失敗時に更新されたユーザーが保存される
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getFailedLoginAttempts()).isEqualTo(1);

            verify(jwtService, never()).generateToken(anyString(), anyMap());
        }

        @Test
        @DisplayName("ロックされたアカウントでログインに失敗する")
        void shouldFailWithLockedAccount() {
            // Given
            String username = "lockeduser";
            User user = User.create(username, "locked@example.com", "Password123!", "ロックユーザー", Role.USER);
            // 3回失敗してロック状態にする（イミュータブルなので結果を受け取る）
            User lockedUser = user
                    .recordFailedLoginAttempt()
                    .recordFailedLoginAttempt()
                    .recordFailedLoginAttempt();

            when(userRepository.findByUsername(username)).thenReturn(Optional.of(lockedUser));

            LoginCommand command = new LoginCommand(username, "Password123!");

            // When
            LoginResult result = authService.execute(command);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).contains("アカウントがロックされています");

            verify(jwtService, never()).generateToken(anyString(), anyMap());
        }

        @Test
        @DisplayName("無効化されたアカウントでログインに失敗する")
        void shouldFailWithDeactivatedAccount() {
            // Given
            String username = "deactivateduser";
            User user = User.create(username, "deactivated@example.com", "Password123!", "無効ユーザー", Role.USER);
            // 無効化する（イミュータブルなので結果を受け取る）
            User deactivatedUser = user.deactivate();

            when(userRepository.findByUsername(username)).thenReturn(Optional.of(deactivatedUser));

            LoginCommand command = new LoginCommand(username, "Password123!");

            // When
            LoginResult result = authService.execute(command);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).contains("アカウントが無効化されています");

            verify(jwtService, never()).generateToken(anyString(), anyMap());
        }
    }

    @Nested
    @DisplayName("ログイン失敗回数管理")
    class FailedAttemptManagement {

        @Test
        @DisplayName("3回連続で失敗するとアカウントがロックされる")
        void shouldLockAccountAfterThreeFailedAttempts() {
            // Given
            String username = "testuser";
            User user = User.create(username, "test@example.com", "CorrectPassword123!", "テストユーザー", Role.USER);

            // 各呼び出しで更新されたユーザーを返すようにする（lenient を使用してネストスタブを許容）
            lenient().when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                // 次回の findByUsername で更新されたユーザーを返す
                lenient().when(userRepository.findByUsername(username)).thenReturn(Optional.of(savedUser));
                return savedUser;
            });

            LoginCommand command = new LoginCommand(username, "WrongPassword");

            // When - 3回失敗
            authService.execute(command);
            authService.execute(command);
            LoginResult result = authService.execute(command);

            // Then
            assertThat(result.success()).isFalse();

            // 3回目の保存でロックされていることを確認
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(3)).save(userCaptor.capture());
            User lastSavedUser = userCaptor.getAllValues().get(2);
            assertThat(lastSavedUser.isLocked()).isTrue();
            assertThat(lastSavedUser.getFailedLoginAttempts()).isEqualTo(3);
        }
    }
}
