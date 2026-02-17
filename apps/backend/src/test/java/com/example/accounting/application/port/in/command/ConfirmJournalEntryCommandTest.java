package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConfirmJournalEntryCommand")
class ConfirmJournalEntryCommandTest {

    @Nested
    @DisplayName("コンストラクタ")
    class Constructor {

        @Test
        @DisplayName("有効な値でインスタンスを生成できる")
        void shouldCreateWithValidValues() {
            ConfirmJournalEntryCommand command = new ConfirmJournalEntryCommand(1, "manager");

            assertThat(command.journalEntryId()).isEqualTo(1);
            assertThat(command.confirmerId()).isEqualTo("manager");
        }

        @Test
        @DisplayName("journalEntryId が null の場合はバリデーションエラーになる")
        void shouldReturnLeftWhenJournalEntryIdIsNull() {
            assertThat(ConfirmJournalEntryCommand.of(null, "manager").getLeft())
                    .isEqualTo("仕訳IDは必須です");
        }

        @Test
        @DisplayName("confirmerId が null の場合はバリデーションエラーになる")
        void shouldReturnLeftWhenConfirmerIdIsNull() {
            assertThat(ConfirmJournalEntryCommand.of(1, null).getLeft())
                    .isEqualTo("確定者IDは必須です");
        }

        @Test
        @DisplayName("confirmerId が空白の場合はバリデーションエラーになる")
        void shouldReturnLeftWhenConfirmerIdIsBlank() {
            assertThat(ConfirmJournalEntryCommand.of(1, "   ").getLeft())
                    .isEqualTo("確定者IDは必須です");
        }
    }
}
