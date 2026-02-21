package com.example.accounting.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GenerateAutoJournalCommand")
class GenerateAutoJournalCommandTest {

    @Nested
    @DisplayName("コンストラクタ")
    class Constructor {

        @Test
        @DisplayName("有効な値でコマンドを生成できる")
        void shouldCreateCommandWithValidValues() {
            Map<String, BigDecimal> amounts = Map.of(
                    "baseAmount", new BigDecimal("1000")
            );

            GenerateAutoJournalCommand command = new GenerateAutoJournalCommand(
                    1L,
                    amounts,
                    LocalDate.of(2026, 2, 21),
                    "月次自動仕訳",
                    "user-001"
            );

            assertThat(command.patternId()).isEqualTo(1L);
            assertThat(command.amounts()).containsEntry("baseAmount", new BigDecimal("1000"));
            assertThat(command.journalDate()).isEqualTo(LocalDate.of(2026, 2, 21));
            assertThat(command.description()).isEqualTo("月次自動仕訳");
            assertThat(command.createdByUserId()).isEqualTo("user-001");
        }

        @Test
        @DisplayName("patternId が null の場合は NullPointerException")
        void shouldThrowWhenPatternIdIsNull() {
            assertThatThrownBy(() -> new GenerateAutoJournalCommand(
                    null,
                    Map.of("baseAmount", new BigDecimal("1000")),
                    LocalDate.of(2026, 2, 21),
                    "月次自動仕訳",
                    "user-001"
            )).isInstanceOf(NullPointerException.class)
                    .hasMessage("パターンIDは必須です");
        }

        @Test
        @DisplayName("amounts が null の場合は NullPointerException")
        void shouldThrowWhenAmountsIsNull() {
            assertThatThrownBy(() -> new GenerateAutoJournalCommand(
                    1L,
                    null,
                    LocalDate.of(2026, 2, 21),
                    "月次自動仕訳",
                    "user-001"
            )).isInstanceOf(NullPointerException.class)
                    .hasMessage("金額パラメータは必須です");
        }

        @Test
        @DisplayName("journalDate が null の場合は NullPointerException")
        void shouldThrowWhenJournalDateIsNull() {
            assertThatThrownBy(() -> new GenerateAutoJournalCommand(
                    1L,
                    Map.of("baseAmount", new BigDecimal("1000")),
                    null,
                    "月次自動仕訳",
                    "user-001"
            )).isInstanceOf(NullPointerException.class)
                    .hasMessage("仕訳日は必須です");
        }

        @Test
        @DisplayName("createdByUserId が null の場合は NullPointerException")
        void shouldThrowWhenCreatedByUserIdIsNull() {
            assertThatThrownBy(() -> new GenerateAutoJournalCommand(
                    1L,
                    Map.of("baseAmount", new BigDecimal("1000")),
                    LocalDate.of(2026, 2, 21),
                    "月次自動仕訳",
                    null
            )).isInstanceOf(NullPointerException.class)
                    .hasMessage("作成者ユーザーIDは必須です");
        }

        @Test
        @DisplayName("amounts は不変コピーされる")
        void shouldDefensivelyCopyAmounts() {
            Map<String, BigDecimal> original = new HashMap<>();
            original.put("baseAmount", new BigDecimal("1000"));

            GenerateAutoJournalCommand command = new GenerateAutoJournalCommand(
                    1L,
                    original,
                    LocalDate.of(2026, 2, 21),
                    "月次自動仕訳",
                    "user-001"
            );

            original.put("tax", new BigDecimal("100"));

            assertThat(command.amounts()).hasSize(1);
            assertThat(command.amounts()).doesNotContainKey("tax");
            assertThatThrownBy(() -> command.amounts().put("fee", BigDecimal.TEN))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
