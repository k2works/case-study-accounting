package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        @DisplayName("username が null の場合は例外をスローする")
        void shouldThrowExceptionForNullUsername() {
            assertThatThrownBy(() -> new RegisterUserCommand(
                    null,
                    "test@example.com",
                    "password123",
                    "Test User",
                    "USER"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ユーザー名は必須です");
        }

        @Test
        @DisplayName("username が空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmptyUsername() {
            assertThatThrownBy(() -> new RegisterUserCommand(
                    "",
                    "test@example.com",
                    "password123",
                    "Test User",
                    "USER"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ユーザー名は必須です");
        }

        @Test
        @DisplayName("username が空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlankUsername() {
            assertThatThrownBy(() -> new RegisterUserCommand(
                    "   ",
                    "test@example.com",
                    "password123",
                    "Test User",
                    "USER"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ユーザー名は必須です");
        }

        @Test
        @DisplayName("email が null の場合は例外をスローする")
        void shouldThrowExceptionForNullEmail() {
            assertThatThrownBy(() -> new RegisterUserCommand(
                    "testuser",
                    null,
                    "password123",
                    "Test User",
                    "USER"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("メールアドレスは必須です");
        }

        @Test
        @DisplayName("email が空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmptyEmail() {
            assertThatThrownBy(() -> new RegisterUserCommand(
                    "testuser",
                    "",
                    "password123",
                    "Test User",
                    "USER"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("メールアドレスは必須です");
        }

        @Test
        @DisplayName("email が空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlankEmail() {
            assertThatThrownBy(() -> new RegisterUserCommand(
                    "testuser",
                    "   ",
                    "password123",
                    "Test User",
                    "USER"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("メールアドレスは必須です");
        }

        @Test
        @DisplayName("password が null の場合は例外をスローする")
        void shouldThrowExceptionForNullPassword() {
            assertThatThrownBy(() -> new RegisterUserCommand(
                    "testuser",
                    "test@example.com",
                    null,
                    "Test User",
                    "USER"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("パスワードは必須です");
        }

        @Test
        @DisplayName("password が空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmptyPassword() {
            assertThatThrownBy(() -> new RegisterUserCommand(
                    "testuser",
                    "test@example.com",
                    "",
                    "Test User",
                    "USER"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("パスワードは必須です");
        }

        @Test
        @DisplayName("password が空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlankPassword() {
            assertThatThrownBy(() -> new RegisterUserCommand(
                    "testuser",
                    "test@example.com",
                    "   ",
                    "Test User",
                    "USER"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("パスワードは必須です");
        }

        @Test
        @DisplayName("password が 8 文字未満の場合は例外をスローする")
        void shouldThrowExceptionForShortPassword() {
            assertThatThrownBy(() -> new RegisterUserCommand(
                    "testuser",
                    "test@example.com",
                    "short",
                    "Test User",
                    "USER"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("パスワードは 8 文字以上で入力してください");
        }

        @Test
        @DisplayName("displayName が null の場合は例外をスローする")
        void shouldThrowExceptionForNullDisplayName() {
            assertThatThrownBy(() -> new RegisterUserCommand(
                    "testuser",
                    "test@example.com",
                    "password123",
                    null,
                    "USER"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("表示名は必須です");
        }

        @Test
        @DisplayName("displayName が空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmptyDisplayName() {
            assertThatThrownBy(() -> new RegisterUserCommand(
                    "testuser",
                    "test@example.com",
                    "password123",
                    "",
                    "USER"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("表示名は必須です");
        }

        @Test
        @DisplayName("displayName が空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlankDisplayName() {
            assertThatThrownBy(() -> new RegisterUserCommand(
                    "testuser",
                    "test@example.com",
                    "password123",
                    "   ",
                    "USER"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("表示名は必須です");
        }

        @Test
        @DisplayName("role が null の場合は例外をスローする")
        void shouldThrowExceptionForNullRole() {
            assertThatThrownBy(() -> new RegisterUserCommand(
                    "testuser",
                    "test@example.com",
                    "password123",
                    "Test User",
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ロールは必須です");
        }

        @Test
        @DisplayName("role が空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmptyRole() {
            assertThatThrownBy(() -> new RegisterUserCommand(
                    "testuser",
                    "test@example.com",
                    "password123",
                    "Test User",
                    ""
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ロールは必須です");
        }

        @Test
        @DisplayName("role が空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlankRole() {
            assertThatThrownBy(() -> new RegisterUserCommand(
                    "testuser",
                    "test@example.com",
                    "password123",
                    "Test User",
                    "   "
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ロールは必須です");
        }
    }
}
