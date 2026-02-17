package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RegisterUserCommand")
class RegisterUserCommandTest {

    @Nested
    @DisplayName("コンストラクタ")
    class Constructor {

        @Test
        @DisplayName("有効な値でインスタンスを生成できる")
        void shouldCreateWithValidValues() {
            RegisterUserCommand command = new RegisterUserCommand(
                    "testuser",
                    "test@example.com",
                    "password123",
                    "Test User",
                    "USER"
            );

            assertThat(command.username()).isEqualTo("testuser");
            assertThat(command.email()).isEqualTo("test@example.com");
            assertThat(command.password()).isEqualTo("password123");
            assertThat(command.displayName()).isEqualTo("Test User");
            assertThat(command.role()).isEqualTo("USER");
        }

        @Test
        @DisplayName("username が null の場合はバリデーションエラーになる")
        void shouldReturnLeftForNullUsername() {
            assertThat(RegisterUserCommand.of(
                    null,
                    "test@example.com",
                    "password123",
                    "Test User",
                    "USER"
            ).getLeft()).isEqualTo("ユーザー名は必須です");
        }

        @Test
        @DisplayName("username が空文字の場合はバリデーションエラーになる")
        void shouldReturnLeftForEmptyUsername() {
            assertThat(RegisterUserCommand.of(
                    "",
                    "test@example.com",
                    "password123",
                    "Test User",
                    "USER"
            ).getLeft()).isEqualTo("ユーザー名は必須です");
        }

        @Test
        @DisplayName("username が空白のみの場合はバリデーションエラーになる")
        void shouldReturnLeftForBlankUsername() {
            assertThat(RegisterUserCommand.of(
                    "   ",
                    "test@example.com",
                    "password123",
                    "Test User",
                    "USER"
            ).getLeft()).isEqualTo("ユーザー名は必須です");
        }

        @Test
        @DisplayName("email が null の場合はバリデーションエラーになる")
        void shouldReturnLeftForNullEmail() {
            assertThat(RegisterUserCommand.of(
                    "testuser",
                    null,
                    "password123",
                    "Test User",
                    "USER"
            ).getLeft()).isEqualTo("メールアドレスは必須です");
        }

        @Test
        @DisplayName("email が空文字の場合はバリデーションエラーになる")
        void shouldReturnLeftForEmptyEmail() {
            assertThat(RegisterUserCommand.of(
                    "testuser",
                    "",
                    "password123",
                    "Test User",
                    "USER"
            ).getLeft()).isEqualTo("メールアドレスは必須です");
        }

        @Test
        @DisplayName("email が空白のみの場合はバリデーションエラーになる")
        void shouldReturnLeftForBlankEmail() {
            assertThat(RegisterUserCommand.of(
                    "testuser",
                    "   ",
                    "password123",
                    "Test User",
                    "USER"
            ).getLeft()).isEqualTo("メールアドレスは必須です");
        }

        @Test
        @DisplayName("password が null の場合はバリデーションエラーになる")
        void shouldReturnLeftForNullPassword() {
            assertThat(RegisterUserCommand.of(
                    "testuser",
                    "test@example.com",
                    null,
                    "Test User",
                    "USER"
            ).getLeft()).isEqualTo("パスワードは必須です");
        }

        @Test
        @DisplayName("password が空文字の場合はバリデーションエラーになる")
        void shouldReturnLeftForEmptyPassword() {
            assertThat(RegisterUserCommand.of(
                    "testuser",
                    "test@example.com",
                    "",
                    "Test User",
                    "USER"
            ).getLeft()).isEqualTo("パスワードは必須です");
        }

        @Test
        @DisplayName("password が空白のみの場合はバリデーションエラーになる")
        void shouldReturnLeftForBlankPassword() {
            assertThat(RegisterUserCommand.of(
                    "testuser",
                    "test@example.com",
                    "   ",
                    "Test User",
                    "USER"
            ).getLeft()).isEqualTo("パスワードは必須です");
        }

        @Test
        @DisplayName("password が 8 文字未満の場合はバリデーションエラーになる")
        void shouldReturnLeftForShortPassword() {
            assertThat(RegisterUserCommand.of(
                    "testuser",
                    "test@example.com",
                    "short",
                    "Test User",
                    "USER"
            ).getLeft()).isEqualTo("パスワードは 8 文字以上で入力してください");
        }

        @Test
        @DisplayName("displayName が null の場合はバリデーションエラーになる")
        void shouldReturnLeftForNullDisplayName() {
            assertThat(RegisterUserCommand.of(
                    "testuser",
                    "test@example.com",
                    "password123",
                    null,
                    "USER"
            ).getLeft()).isEqualTo("表示名は必須です");
        }

        @Test
        @DisplayName("displayName が空文字の場合はバリデーションエラーになる")
        void shouldReturnLeftForEmptyDisplayName() {
            assertThat(RegisterUserCommand.of(
                    "testuser",
                    "test@example.com",
                    "password123",
                    "",
                    "USER"
            ).getLeft()).isEqualTo("表示名は必須です");
        }

        @Test
        @DisplayName("displayName が空白のみの場合はバリデーションエラーになる")
        void shouldReturnLeftForBlankDisplayName() {
            assertThat(RegisterUserCommand.of(
                    "testuser",
                    "test@example.com",
                    "password123",
                    "   ",
                    "USER"
            ).getLeft()).isEqualTo("表示名は必須です");
        }

        @Test
        @DisplayName("role が null の場合はバリデーションエラーになる")
        void shouldReturnLeftForNullRole() {
            assertThat(RegisterUserCommand.of(
                    "testuser",
                    "test@example.com",
                    "password123",
                    "Test User",
                    null
            ).getLeft()).isEqualTo("ロールは必須です");
        }

        @Test
        @DisplayName("role が空文字の場合はバリデーションエラーになる")
        void shouldReturnLeftForEmptyRole() {
            assertThat(RegisterUserCommand.of(
                    "testuser",
                    "test@example.com",
                    "password123",
                    "Test User",
                    ""
            ).getLeft()).isEqualTo("ロールは必須です");
        }

        @Test
        @DisplayName("role が空白のみの場合はバリデーションエラーになる")
        void shouldReturnLeftForBlankRole() {
            assertThat(RegisterUserCommand.of(
                    "testuser",
                    "test@example.com",
                    "password123",
                    "Test User",
                    "   "
            ).getLeft()).isEqualTo("ロールは必須です");
        }
    }
}
