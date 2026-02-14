package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        @DisplayName("journalEntryId が null の場合は例外をスローする")
        void shouldThrowWhenJournalEntryIdIsNull() {
            assertThatThrownBy(() -> new ConfirmJournalEntryCommand(null, "manager"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("仕訳IDは必須です");
        }

        @Test
        @DisplayName("confirmerId が null の場合は例外をスローする")
        void shouldThrowWhenConfirmerIdIsNull() {
            assertThatThrownBy(() -> new ConfirmJournalEntryCommand(1, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("確定者IDは必須です");
        }

        @Test
        @DisplayName("confirmerId が空白の場合は例外をスローする")
        void shouldThrowWhenConfirmerIdIsBlank() {
            assertThatThrownBy(() -> new ConfirmJournalEntryCommand(1, "   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("確定者IDは必須です");
        }
    }
}
