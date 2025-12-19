package com.example.accounting.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * User エンティティのテスト
 */
@DisplayName("User エンティティ")
class UserTest {

    @Nested
    @DisplayName("ユーザー作成")
    class Create {

        @Test
        @DisplayName("有効な情報でユーザーを作成できる")
        void shouldCreateUserWithValidInfo() {
            // Given
            String username = "testuser";
            String email = "test@example.com";
            String password = "SecurePass123!";
            String displayName = "テストユーザー";
            Role role = Role.USER;

            // When
            User user = User.create(username, email, password, displayName, role);

            // Then
            assertThat(user.getUsername()).isEqualTo(username);
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getDisplayName()).isEqualTo(displayName);
            assertThat(user.getRole()).isEqualTo(role);
            assertThat(user.isActive()).isTrue();
            assertThat(user.getFailedLoginAttempts()).isZero();
            assertThat(user.isLocked()).isFalse();
        }

        @Test
        @DisplayName("パスワードはハッシュ化されて保存される")
        void shouldHashPassword() {
            // Given
            String rawPassword = "SecurePass123!";

            // When
            User user = User.create("testuser", "test@example.com", rawPassword, "テスト", Role.USER);

            // Then
            assertThat(user.getPassword()).isNotEqualTo(rawPassword);
            assertThat(user.getPassword()).startsWith("$2a$"); // BCrypt format
        }

        @Test
        @DisplayName("ユーザー名が null の場合は例外が発生する")
        void shouldThrowExceptionWhenUsernameIsNull() {
            assertThatThrownBy(() ->
                User.create(null, "test@example.com", "Password123!", "テスト", Role.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ユーザー名");
        }

        @Test
        @DisplayName("ユーザー名が空の場合は例外が発生する")
        void shouldThrowExceptionWhenUsernameIsEmpty() {
            assertThatThrownBy(() ->
                User.create("", "test@example.com", "Password123!", "テスト", Role.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ユーザー名");
        }

        @Test
        @DisplayName("メールアドレスが無効な形式の場合は例外が発生する")
        void shouldThrowExceptionWhenEmailIsInvalid() {
            assertThatThrownBy(() ->
                User.create("testuser", "invalid-email", "Password123!", "テスト", Role.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("メールアドレス");
        }
    }

    @Nested
    @DisplayName("パスワード検証")
    class PasswordVerification {

        @Test
        @DisplayName("正しいパスワードで認証に成功する")
        void shouldAuthenticateWithCorrectPassword() {
            // Given
            String rawPassword = "SecurePass123!";
            User user = User.create("testuser", "test@example.com", rawPassword, "テスト", Role.USER);

            // When & Then
            assertThat(user.verifyPassword(rawPassword)).isTrue();
        }

        @Test
        @DisplayName("間違ったパスワードで認証に失敗する")
        void shouldFailAuthenticationWithWrongPassword() {
            // Given
            User user = User.create("testuser", "test@example.com", "SecurePass123!", "テスト", Role.USER);

            // When & Then
            assertThat(user.verifyPassword("WrongPassword")).isFalse();
        }
    }

    @Nested
    @DisplayName("ログイン試行回数管理")
    class LoginAttempts {

        @Test
        @DisplayName("ログイン失敗時に失敗回数がインクリメントされる")
        void shouldIncrementFailedAttemptsOnLoginFailure() {
            // Given
            User user = User.create("testuser", "test@example.com", "Password123!", "テスト", Role.USER);

            // When
            user.recordFailedLoginAttempt();

            // Then
            assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        }

        @Test
        @DisplayName("3回連続でログイン失敗した場合、アカウントがロックされる")
        void shouldLockAccountAfterThreeFailedAttempts() {
            // Given
            User user = User.create("testuser", "test@example.com", "Password123!", "テスト", Role.USER);

            // When
            user.recordFailedLoginAttempt();
            user.recordFailedLoginAttempt();
            user.recordFailedLoginAttempt();

            // Then
            assertThat(user.isLocked()).isTrue();
        }

        @Test
        @DisplayName("ログイン成功時に失敗回数がリセットされる")
        void shouldResetFailedAttemptsOnSuccessfulLogin() {
            // Given
            User user = User.create("testuser", "test@example.com", "Password123!", "テスト", Role.USER);
            user.recordFailedLoginAttempt();
            user.recordFailedLoginAttempt();

            // When
            user.recordSuccessfulLogin();

            // Then
            assertThat(user.getFailedLoginAttempts()).isZero();
            assertThat(user.getLastLoginAt()).isNotNull();
        }

        @Test
        @DisplayName("ロックされたアカウントではログインできない")
        void shouldNotAllowLoginWhenLocked() {
            // Given
            User user = User.create("testuser", "test@example.com", "Password123!", "テスト", Role.USER);
            user.recordFailedLoginAttempt();
            user.recordFailedLoginAttempt();
            user.recordFailedLoginAttempt();

            // When & Then
            assertThat(user.canLogin()).isFalse();
        }
    }

    @Nested
    @DisplayName("アカウント状態管理")
    class AccountStatus {

        @Test
        @DisplayName("アカウントを無効化できる")
        void shouldDeactivateAccount() {
            // Given
            User user = User.create("testuser", "test@example.com", "Password123!", "テスト", Role.USER);

            // When
            user.deactivate();

            // Then
            assertThat(user.isActive()).isFalse();
        }

        @Test
        @DisplayName("無効化されたアカウントではログインできない")
        void shouldNotAllowLoginWhenDeactivated() {
            // Given
            User user = User.create("testuser", "test@example.com", "Password123!", "テスト", Role.USER);
            user.deactivate();

            // When & Then
            assertThat(user.canLogin()).isFalse();
        }

        @Test
        @DisplayName("ロックされたアカウントを解除できる")
        void shouldUnlockAccount() {
            // Given
            User user = User.create("testuser", "test@example.com", "Password123!", "テスト", Role.USER);
            user.recordFailedLoginAttempt();
            user.recordFailedLoginAttempt();
            user.recordFailedLoginAttempt();

            // When
            user.unlock();

            // Then
            assertThat(user.isLocked()).isFalse();
            assertThat(user.getFailedLoginAttempts()).isZero();
        }
    }
}
