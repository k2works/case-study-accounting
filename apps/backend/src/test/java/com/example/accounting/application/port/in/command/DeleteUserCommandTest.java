package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DeleteUserCommand")
class DeleteUserCommandTest {

    @Nested
    @DisplayName("コンストラクタ")
    class Constructor {

        @Test
        @DisplayName("有効なユーザーIDでコマンドを作成できる")
        void shouldCreateCommandWithValidUserId() {
            // given
            String userId = "user-123";

            // when
            DeleteUserCommand command = new DeleteUserCommand(userId);

            // then
            assertThat(command.userId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("ユーザーIDがnullの場合はバリデーションエラーになる")
        void shouldReturnLeftWhenUserIdIsNull() {
            assertThat(DeleteUserCommand.of(null).getLeft())
                    .isEqualTo("ユーザーIDは必須です");
        }

        @Test
        @DisplayName("ユーザーIDが空文字の場合はバリデーションエラーになる")
        void shouldReturnLeftWhenUserIdIsEmpty() {
            assertThat(DeleteUserCommand.of("").getLeft())
                    .isEqualTo("ユーザーIDは必須です");
        }

        @Test
        @DisplayName("ユーザーIDが空白のみの場合はバリデーションエラーになる")
        void shouldReturnLeftWhenUserIdIsBlank() {
            assertThat(DeleteUserCommand.of("   ").getLeft())
                    .isEqualTo("ユーザーIDは必須です");
        }
    }
}
