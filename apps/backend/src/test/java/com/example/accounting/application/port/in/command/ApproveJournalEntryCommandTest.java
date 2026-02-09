package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        @DisplayName("journalEntryId が null の場合は例外をスローする")
        void shouldThrowWhenJournalEntryIdIsNull() {
            assertThatThrownBy(() -> new ApproveJournalEntryCommand(null, "manager"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("仕訳IDは必須です");
        }

        @Test
        @DisplayName("approverId が null の場合は例外をスローする")
        void shouldThrowWhenApproverIdIsNull() {
            assertThatThrownBy(() -> new ApproveJournalEntryCommand(1, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("承認者IDは必須です");
        }

        @Test
        @DisplayName("approverId が空白の場合は例外をスローする")
        void shouldThrowWhenApproverIdIsBlank() {
            assertThatThrownBy(() -> new ApproveJournalEntryCommand(1, "   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("承認者IDは必須です");
        }
    }
}
