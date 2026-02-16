package com.example.accounting.application.port.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DeleteAccountCommand")
class DeleteAccountCommandTest {

    @Test
    @DisplayName("有効な ID でコマンドを作成できる")
    void shouldCreateCommandWithValidId() {
        DeleteAccountCommand command = new DeleteAccountCommand(1);

        assertThat(command.accountId()).isEqualTo(1);
    }

    @Test
    @DisplayName("accountId が null の場合はバリデーションエラーになる")
    void shouldReturnLeftWhenAccountIdIsNull() {
        assertThat(DeleteAccountCommand.of(null).getLeft())
                .isEqualTo("勘定科目IDは必須です");
    }
}
