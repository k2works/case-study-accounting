package com.example.accounting.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
            User updated = user.recordFailedLoginAttempt();

            // Then
            assertThat(updated.getFailedLoginAttempts()).isEqualTo(1);
            // 元のインスタンスは変更されない（イミュータブル）
            assertThat(user.getFailedLoginAttempts()).isZero();
        }

        @Test
        @DisplayName("3回連続でログイン失敗した場合、アカウントがロックされる")
        void shouldLockAccountAfterThreeFailedAttempts() {
            // Given
            User user = User.create("testuser", "test@example.com", "Password123!", "テスト", Role.USER);

            // When
            User updated = user
                    .recordFailedLoginAttempt()
                    .recordFailedLoginAttempt()
                    .recordFailedLoginAttempt();

            // Then
            assertThat(updated.isLocked()).isTrue();
            assertThat(updated.getFailedLoginAttempts()).isEqualTo(3);
        }

        @Test
        @DisplayName("ログイン成功時に失敗回数がリセットされる")
        void shouldResetFailedAttemptsOnSuccessfulLogin() {
            // Given
            User user = User.create("testuser", "test@example.com", "Password123!", "テスト", Role.USER);
            User withFailures = user
                    .recordFailedLoginAttempt()
                    .recordFailedLoginAttempt();

            // When
            User updated = withFailures.recordSuccessfulLogin();

            // Then
            assertThat(updated.getFailedLoginAttempts()).isZero();
            assertThat(updated.getLastLoginAt()).isNotNull();
        }

        @Test
        @DisplayName("ロックされたアカウントではログインできない")
        void shouldNotAllowLoginWhenLocked() {
            // Given
            User user = User.create("testuser", "test@example.com", "Password123!", "テスト", Role.USER);
            User lockedUser = user
                    .recordFailedLoginAttempt()
                    .recordFailedLoginAttempt()
                    .recordFailedLoginAttempt();

            // When & Then
            assertThat(lockedUser.canLogin()).isFalse();
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
            User deactivated = user.deactivate();

            // Then
            assertThat(deactivated.isActive()).isFalse();
            // 元のインスタンスは変更されない（イミュータブル）
            assertThat(user.isActive()).isTrue();
        }

        @Test
        @DisplayName("無効化されたアカウントではログインできない")
        void shouldNotAllowLoginWhenDeactivated() {
            // Given
            User user = User.create("testuser", "test@example.com", "Password123!", "テスト", Role.USER);
            User deactivated = user.deactivate();

            // When & Then
            assertThat(deactivated.canLogin()).isFalse();
        }

        @Test
        @DisplayName("ロックされたアカウントを解除できる")
        void shouldUnlockAccount() {
            // Given
            User user = User.create("testuser", "test@example.com", "Password123!", "テスト", Role.USER);
            User lockedUser = user
                    .recordFailedLoginAttempt()
                    .recordFailedLoginAttempt()
                    .recordFailedLoginAttempt();

            // When
            User unlocked = lockedUser.unlock();

            // Then
            assertThat(unlocked.isLocked()).isFalse();
            assertThat(unlocked.getFailedLoginAttempts()).isZero();
        }
    }

    @Nested
    @DisplayName("イミュータブル設計")
    class ImmutableDesign {

        @Test
        @DisplayName("状態変更メソッドは元のインスタンスを変更しない")
        void shouldNotModifyOriginalInstance() {
            // Given
            User original = User.create("testuser", "test@example.com", "Password123!", "テスト", Role.USER);

            // When
            User afterFailedLogin = original.recordFailedLoginAttempt();
            User afterDeactivate = original.deactivate();
            User afterRoleChange = original.changeRole(Role.ADMIN);

            // Then - 元のインスタンスは変更されない
            assertThat(original.getFailedLoginAttempts()).isZero();
            assertThat(original.isActive()).isTrue();
            assertThat(original.getRole()).isEqualTo(Role.USER);

            // 新しいインスタンスは変更されている
            assertThat(afterFailedLogin.getFailedLoginAttempts()).isEqualTo(1);
            assertThat(afterDeactivate.isActive()).isFalse();
            assertThat(afterRoleChange.getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("メソッドチェーンで複数の変更を適用できる")
        void shouldSupportMethodChaining() {
            // Given
            User user = User.create("testuser", "test@example.com", "Password123!", "テスト", Role.USER);

            // When
            User updated = user
                    .recordFailedLoginAttempt()
                    .recordFailedLoginAttempt()
                    .recordFailedLoginAttempt()
                    .unlock()
                    .changeRole(Role.ADMIN);

            // Then
            assertThat(updated.isLocked()).isFalse();
            assertThat(updated.getFailedLoginAttempts()).isZero();
            assertThat(updated.getRole()).isEqualTo(Role.ADMIN);
        }
    }
}
