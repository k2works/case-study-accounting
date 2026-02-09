package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SubmitForApprovalCommand")
class SubmitForApprovalCommandTest {

    @Nested
    @DisplayName("コンストラクタ")
    class Constructor {

        @Test
        @DisplayName("有効な値でインスタンスを生成できる")
        void shouldCreateWithValidValues() {
            SubmitForApprovalCommand command = new SubmitForApprovalCommand(1);

            assertThat(command.journalEntryId()).isEqualTo(1);
        }

        @Test
        @DisplayName("journalEntryId が null の場合は例外をスローする")
        void shouldThrowWhenJournalEntryIdIsNull() {
            assertThatThrownBy(() -> new SubmitForApprovalCommand(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("仕訳IDは必須です");
        }
    }
}
