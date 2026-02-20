package com.example.accounting.domain.model.auto_journal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("自動仕訳パターン明細")
class AutoJournalPatternItemTest {

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("借方明細を生成できる")
        void shouldCreateDebitItem() {
            AutoJournalPatternItem item = AutoJournalPatternItem.create(1, "D", "1100", "amount", "売上");

            assertThat(item.getLineNumber()).isEqualTo(1);
            assertThat(item.getDebitCreditType()).isEqualTo("D");
            assertThat(item.getAccountCode()).isEqualTo("1100");
            assertThat(item.getAmountFormula()).isEqualTo("amount");
            assertThat(item.getDescriptionTemplate()).isEqualTo("売上");
        }

        @Test
        @DisplayName("貸方明細を生成できる")
        void shouldCreateCreditItem() {
            AutoJournalPatternItem item = AutoJournalPatternItem.create(2, "C", "4000", "amount", null);

            assertThat(item.getDebitCreditType()).isEqualTo("C");
            assertThat(item.getDescriptionTemplate()).isNull();
        }

        @Test
        @DisplayName("lineNumber が null の場合は例外をスローする")
        void shouldThrowWhenLineNumberIsNull() {
            assertThatThrownBy(() -> AutoJournalPatternItem.create(null, "D", "1100", "amount", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("行番号は必須です");
        }

        @Test
        @DisplayName("debitCreditType が null の場合は例外をスローする")
        void shouldThrowWhenDebitCreditTypeIsNull() {
            assertThatThrownBy(() -> AutoJournalPatternItem.create(1, null, "1100", "amount", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("貸借区分は必須です");
        }

        @Test
        @DisplayName("accountCode が null の場合は例外をスローする")
        void shouldThrowWhenAccountCodeIsNull() {
            assertThatThrownBy(() -> AutoJournalPatternItem.create(1, "D", null, "amount", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("勘定科目コードは必須です");
        }

        @Test
        @DisplayName("amountFormula が null の場合は例外をスローする")
        void shouldThrowWhenAmountFormulaIsNull() {
            assertThatThrownBy(() -> AutoJournalPatternItem.create(1, "D", "1100", null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("金額計算式は必須です");
        }

        @Test
        @DisplayName("不正な貸借区分の場合は例外をスローする")
        void shouldThrowWhenDebitCreditTypeIsInvalid() {
            assertThatThrownBy(() -> AutoJournalPatternItem.create(1, "X", "1100", "amount", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("貸借区分は 'D' または 'C' である必要があります");
        }
    }

    @Nested
    @DisplayName("reconstruct")
    class Reconstruct {

        @Test
        @DisplayName("バリデーションなしで再構築できる")
        void shouldReconstructWithoutValidation() {
            AutoJournalPatternItem item = AutoJournalPatternItem.reconstruct(1, "D", "1100", "amount", "売上");

            assertThat(item.getLineNumber()).isEqualTo(1);
            assertThat(item.getDebitCreditType()).isEqualTo("D");
            assertThat(item.getAccountCode()).isEqualTo("1100");
            assertThat(item.getAmountFormula()).isEqualTo("amount");
            assertThat(item.getDescriptionTemplate()).isEqualTo("売上");
        }
    }
}
