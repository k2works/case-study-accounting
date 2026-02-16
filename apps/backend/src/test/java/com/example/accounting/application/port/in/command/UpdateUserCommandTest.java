package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
            assertThat(UpdateUserCommand.of(
                    "user-1",
                    "更新ユーザー",
                    null,
                    "USER"
            ).isRight()).isTrue();
        }

        @Test
        @DisplayName("password が空文字でも生成できる")
        void shouldAllowEmptyPassword() {
            assertThat(UpdateUserCommand.of(
                    "user-1",
                    "更新ユーザー",
                    "",
                    "USER"
            ).isRight()).isTrue();
        }

        @Test
        @DisplayName("userId が null の場合はバリデーションエラーになる")
        void shouldReturnLeftForNullUserId() {
            assertThat(UpdateUserCommand.of(
                    null,
                    "更新ユーザー",
                    "Password123!",
                    "USER"
            ).getLeft()).isEqualTo("ユーザーIDは必須です");
        }

        @Test
        @DisplayName("userId が空文字の場合はバリデーションエラーになる")
        void shouldReturnLeftForEmptyUserId() {
            assertThat(UpdateUserCommand.of(
                    "",
                    "更新ユーザー",
                    "Password123!",
                    "USER"
            ).getLeft()).isEqualTo("ユーザーIDは必須です");
        }

        @Test
        @DisplayName("displayName が null の場合はバリデーションエラーになる")
        void shouldReturnLeftForNullDisplayName() {
            assertThat(UpdateUserCommand.of(
                    "user-1",
                    null,
                    "Password123!",
                    "USER"
            ).getLeft()).isEqualTo("表示名は必須です");
        }

        @Test
        @DisplayName("displayName が空白のみの場合はバリデーションエラーになる")
        void shouldReturnLeftForBlankDisplayName() {
            assertThat(UpdateUserCommand.of(
                    "user-1",
                    "   ",
                    "Password123!",
                    "USER"
            ).getLeft()).isEqualTo("表示名は必須です");
        }

        @Test
        @DisplayName("role が null の場合はバリデーションエラーになる")
        void shouldReturnLeftForNullRole() {
            assertThat(UpdateUserCommand.of(
                    "user-1",
                    "更新ユーザー",
                    "Password123!",
                    null
            ).getLeft()).isEqualTo("ロールは必須です");
        }

        @Test
        @DisplayName("role が空白のみの場合はバリデーションエラーになる")
        void shouldReturnLeftForBlankRole() {
            assertThat(UpdateUserCommand.of(
                    "user-1",
                    "更新ユーザー",
                    "Password123!",
                    "   "
            ).getLeft()).isEqualTo("ロールは必須です");
        }
    }
}
