package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UpdateUserCommand")
class UpdateUserCommandTest {

    @Nested
    @DisplayName("コンストラクタ")
    class Constructor {

        @Test
        @DisplayName("有効な値でインスタンスを生成できる")
        void shouldCreateWithValidValues() {
            UpdateUserCommand command = new UpdateUserCommand(
                    "user-1",
                    "更新ユーザー",
                    "Password123!",
                    "MANAGER"
            );

            assertThat(command.userId()).isEqualTo("user-1");
            assertThat(command.displayName()).isEqualTo("更新ユーザー");
            assertThat(command.password()).isEqualTo("Password123!");
            assertThat(command.role()).isEqualTo("MANAGER");
        }

        @Test
        @DisplayName("password が null でも生成できる")
        void shouldAllowNullPassword() {
            assertThatCode(() -> new UpdateUserCommand(
                    "user-1",
                    "更新ユーザー",
                    null,
                    "USER"
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("password が空文字でも生成できる")
        void shouldAllowEmptyPassword() {
            assertThatCode(() -> new UpdateUserCommand(
                    "user-1",
                    "更新ユーザー",
                    "",
                    "USER"
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("userId が null の場合は例外をスローする")
        void shouldThrowExceptionForNullUserId() {
            assertThatThrownBy(() -> new UpdateUserCommand(
                    null,
                    "更新ユーザー",
                    "Password123!",
                    "USER"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ユーザーIDは必須です");
        }

        @Test
        @DisplayName("userId が空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmptyUserId() {
            assertThatThrownBy(() -> new UpdateUserCommand(
                    "",
                    "更新ユーザー",
                    "Password123!",
                    "USER"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ユーザーIDは必須です");
        }

        @Test
        @DisplayName("displayName が null の場合は例外をスローする")
        void shouldThrowExceptionForNullDisplayName() {
            assertThatThrownBy(() -> new UpdateUserCommand(
                    "user-1",
                    null,
                    "Password123!",
                    "USER"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("表示名は必須です");
        }

        @Test
        @DisplayName("displayName が空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlankDisplayName() {
            assertThatThrownBy(() -> new UpdateUserCommand(
                    "user-1",
                    "   ",
                    "Password123!",
                    "USER"
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("表示名は必須です");
        }

        @Test
        @DisplayName("role が null の場合は例外をスローする")
        void shouldThrowExceptionForNullRole() {
            assertThatThrownBy(() -> new UpdateUserCommand(
                    "user-1",
                    "更新ユーザー",
                    "Password123!",
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ロールは必須です");
        }

        @Test
        @DisplayName("role が空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlankRole() {
            assertThatThrownBy(() -> new UpdateUserCommand(
                    "user-1",
                    "更新ユーザー",
                    "Password123!",
                    "   "
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ロールは必須です");
        }
    }
}
