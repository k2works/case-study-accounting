package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginCommand")
class LoginCommandTest {

    @Nested
    @DisplayName("コンストラクタ")
    class Constructor {

        @Test
        @DisplayName("有効な値でインスタンスを生成できる")
        void shouldCreateWithValidValues() {
            LoginCommand command = new LoginCommand("testuser", "password123");

            assertThat(command.username()).isEqualTo("testuser");
            assertThat(command.password()).isEqualTo("password123");
        }

        @Test
        @DisplayName("username が null の場合はバリデーションエラーになる")
        void shouldReturnLeftForNullUsername() {
            assertThat(LoginCommand.of(null, "password123").getLeft())
                    .isEqualTo("ユーザー名は必須です");
        }

        @Test
        @DisplayName("username が空文字の場合はバリデーションエラーになる")
        void shouldReturnLeftForEmptyUsername() {
            assertThat(LoginCommand.of("", "password123").getLeft())
                    .isEqualTo("ユーザー名は必須です");
        }

        @Test
        @DisplayName("username が空白のみの場合はバリデーションエラーになる")
        void shouldReturnLeftForBlankUsername() {
            assertThat(LoginCommand.of("   ", "password123").getLeft())
                    .isEqualTo("ユーザー名は必須です");
        }

        @Test
        @DisplayName("password が null の場合はバリデーションエラーになる")
        void shouldReturnLeftForNullPassword() {
            assertThat(LoginCommand.of("testuser", null).getLeft())
                    .isEqualTo("パスワードは必須です");
        }

        @Test
        @DisplayName("password が空文字の場合はバリデーションエラーになる")
        void shouldReturnLeftForEmptyPassword() {
            assertThat(LoginCommand.of("testuser", "").getLeft())
                    .isEqualTo("パスワードは必須です");
        }

        @Test
        @DisplayName("password が空白のみの場合はバリデーションエラーになる")
        void shouldReturnLeftForBlankPassword() {
            assertThat(LoginCommand.of("testuser", "   ").getLeft())
                    .isEqualTo("パスワードは必須です");
        }
    }
}
