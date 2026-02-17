package com.example.accounting.domain.model.journal;

import com.example.accounting.domain.model.account.AccountId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JournalEntryLine")
class JournalEntryLineTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("借方金額のみ設定された明細を作成できる")
        void shouldCreateDebitLine() {
            JournalEntryLine line = JournalEntryLine.of(
                    1,
                    AccountId.of(10),
                    Money.of(new BigDecimal("1000")),
                    null
            );

            assertThat(line.isDebit()).isTrue();
            assertThat(line.isCredit()).isFalse();
        }

        @Test
        @DisplayName("貸方金額のみ設定された明細を作成できる")
        void shouldCreateCreditLine() {
            JournalEntryLine line = JournalEntryLine.of(
                    2,
                    AccountId.of(20),
                    null,
                    Money.of(new BigDecimal("500"))
            );

            assertThat(line.isDebit()).isFalse();
            assertThat(line.isCredit()).isTrue();
        }
    }

    @Nested
    @DisplayName("validated")
    class Validated {

        @Test
        @DisplayName("有効な借方明細で Right を返す")
        void shouldReturnRightForValidDebitLine() {
            var result = JournalEntryLine.validated(
                    1, AccountId.of(10), Money.of(new BigDecimal("1000")), null);
            assertThat(result.isRight()).isTrue();
            assertThat(result.get().isDebit()).isTrue();
        }

        @Test
        @DisplayName("借方と貸方の両方が設定されている場合はエラーメッセージを返す")
        void shouldReturnLeftWhenBothAmountsSet() {
            AccountId accountId = AccountId.of(10);
            Money debit = Money.of(new BigDecimal("100"));
            Money credit = Money.of(new BigDecimal("100"));

            assertThat(JournalEntryLine.validated(1, accountId, debit, credit).getLeft())
                    .contains("借方または貸方");
        }

        @Test
        @DisplayName("借方と貸方の両方が null の場合はエラーメッセージを返す")
        void shouldReturnLeftWhenBothAmountsNull() {
            AccountId accountId = AccountId.of(10);

            assertThat(JournalEntryLine.validated(1, accountId, null, null).getLeft())
                    .contains("借方または貸方");
        }

        @Test
        @DisplayName("lineNumber が null の場合はエラーメッセージを返す")
        void shouldReturnLeftWhenLineNumberIsNull() {
            AccountId accountId = AccountId.of(10);
            Money debit = Money.of(new BigDecimal("100"));

            assertThat(JournalEntryLine.validated(null, accountId, debit, null).getLeft())
                    .contains("行番号");
        }

        @Test
        @DisplayName("accountId が null の場合はエラーメッセージを返す")
        void shouldReturnLeftWhenAccountIdIsNull() {
            Money debit = Money.of(new BigDecimal("100"));

            assertThat(JournalEntryLine.validated(1, null, debit, null).getLeft())
                    .contains("勘定科目 ID");
        }
    }
}
