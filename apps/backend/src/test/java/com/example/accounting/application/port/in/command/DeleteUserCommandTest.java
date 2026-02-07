package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        @DisplayName("ユーザーIDがnullの場合は例外をスローする")
        void shouldThrowExceptionWhenUserIdIsNull() {
            assertThatThrownBy(() -> new DeleteUserCommand(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ユーザーIDは必須です");
        }

        @Test
        @DisplayName("ユーザーIDが空文字の場合は例外をスローする")
        void shouldThrowExceptionWhenUserIdIsEmpty() {
            assertThatThrownBy(() -> new DeleteUserCommand(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ユーザーIDは必須です");
        }

        @Test
        @DisplayName("ユーザーIDが空白のみの場合は例外をスローする")
        void shouldThrowExceptionWhenUserIdIsBlank() {
            assertThatThrownBy(() -> new DeleteUserCommand("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ユーザーIDは必須です");
        }
    }
}
