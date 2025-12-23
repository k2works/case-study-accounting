package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        @DisplayName("username が null の場合は例外をスローする")
        void shouldThrowExceptionForNullUsername() {
            assertThatThrownBy(() -> new LoginCommand(null, "password123"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ユーザー名は必須です");
        }

        @Test
        @DisplayName("username が空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmptyUsername() {
            assertThatThrownBy(() -> new LoginCommand("", "password123"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ユーザー名は必須です");
        }

        @Test
        @DisplayName("username が空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlankUsername() {
            assertThatThrownBy(() -> new LoginCommand("   ", "password123"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ユーザー名は必須です");
        }

        @Test
        @DisplayName("password が null の場合は例外をスローする")
        void shouldThrowExceptionForNullPassword() {
            assertThatThrownBy(() -> new LoginCommand("testuser", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("パスワードは必須です");
        }

        @Test
        @DisplayName("password が空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmptyPassword() {
            assertThatThrownBy(() -> new LoginCommand("testuser", ""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("パスワードは必須です");
        }

        @Test
        @DisplayName("password が空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlankPassword() {
            assertThatThrownBy(() -> new LoginCommand("testuser", "   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("パスワードは必須です");
        }
    }
}
