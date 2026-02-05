package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UpdateJournalEntryCommand")
class UpdateJournalEntryCommandTest {

    @Nested
    @DisplayName("コンストラクタ")
    class Constructor {

        @Test
        @DisplayName("有効な値でインスタンスを生成できる")
        void shouldCreateWithValidValues() {
            List<UpdateJournalEntryCommand.JournalEntryLineInput> lines = List.of(
                    new UpdateJournalEntryCommand.JournalEntryLineInput(
                            1,
                            100,
                            new BigDecimal("1000"),
                            null
                    )
            );

            UpdateJournalEntryCommand command = new UpdateJournalEntryCommand(
                    10,
                    LocalDate.of(2024, 2, 1),
                    "摘要",
                    lines,
                    2
            );

            assertThat(command.journalEntryId()).isEqualTo(10);
            assertThat(command.journalDate()).isEqualTo(LocalDate.of(2024, 2, 1));
            assertThat(command.description()).isEqualTo("摘要");
            assertThat(command.lines()).hasSize(1);
            assertThat(command.version()).isEqualTo(2);
        }

        @Test
        @DisplayName("lines が null の場合は空リストになる")
        void shouldUseEmptyListWhenLinesNull() {
            UpdateJournalEntryCommand command = new UpdateJournalEntryCommand(
                    10,
                    LocalDate.of(2024, 2, 1),
                    "摘要",
                    null,
                    1
            );

            assertThat(command.lines()).isEmpty();
        }

        @Test
        @DisplayName("lines は防御的コピーされる")
        void shouldDefensivelyCopyLines() {
            List<UpdateJournalEntryCommand.JournalEntryLineInput> lines = new ArrayList<>();
            lines.add(new UpdateJournalEntryCommand.JournalEntryLineInput(
                    1,
                    100,
                    new BigDecimal("1000"),
                    null
            ));

            UpdateJournalEntryCommand command = new UpdateJournalEntryCommand(
                    10,
                    LocalDate.of(2024, 2, 1),
                    "摘要",
                    lines,
                    1
            );

            lines.add(new UpdateJournalEntryCommand.JournalEntryLineInput(
                    2,
                    200,
                    null,
                    new BigDecimal("1000")
            ));

            assertThat(command.lines()).hasSize(1);
            UpdateJournalEntryCommand.JournalEntryLineInput firstLine = lines.get(0);
            assertThatThrownBy(() -> command.lines().add(firstLine))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
