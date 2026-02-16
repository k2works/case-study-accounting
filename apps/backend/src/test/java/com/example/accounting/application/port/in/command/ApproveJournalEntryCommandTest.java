package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApproveJournalEntryCommand")
class ApproveJournalEntryCommandTest {

    @Nested
    @DisplayName("コンストラクタ")
    class Constructor {

        @Test
        @DisplayName("有効な値でインスタンスを生成できる")
        void shouldCreateWithValidValues() {
            ApproveJournalEntryCommand command = new ApproveJournalEntryCommand(1, "manager");

            assertThat(command.journalEntryId()).isEqualTo(1);
            assertThat(command.approverId()).isEqualTo("manager");
        }

        @Test
        @DisplayName("journalEntryId が null の場合はバリデーションエラーになる")
        void shouldReturnLeftWhenJournalEntryIdIsNull() {
            assertThat(ApproveJournalEntryCommand.of(null, "manager").getLeft())
                    .isEqualTo("仕訳IDは必須です");
        }

        @Test
        @DisplayName("approverId が null の場合はバリデーションエラーになる")
        void shouldReturnLeftWhenApproverIdIsNull() {
            assertThat(ApproveJournalEntryCommand.of(1, null).getLeft())
                    .isEqualTo("承認者IDは必須です");
        }

        @Test
        @DisplayName("approverId が空白の場合はバリデーションエラーになる")
        void shouldReturnLeftWhenApproverIdIsBlank() {
            assertThat(ApproveJournalEntryCommand.of(1, "   ").getLeft())
                    .isEqualTo("承認者IDは必須です");
        }
    }
}
