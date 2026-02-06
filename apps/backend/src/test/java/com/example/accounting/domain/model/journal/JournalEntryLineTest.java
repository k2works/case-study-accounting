package com.example.accounting.domain.model.journal;

import com.example.accounting.domain.model.account.AccountId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JournalEntryLine")
class JournalEntryLineTest {

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

    @Test
    @DisplayName("借方と貸方の両方が設定されている場合は例外をスローする")
    void shouldThrowExceptionWhenBothAmountsSet() {
        AccountId accountId = AccountId.of(10);
        Money debit = Money.of(new BigDecimal("100"));
        Money credit = Money.of(new BigDecimal("100"));

        assertThatThrownBy(() -> JournalEntryLine.of(1, accountId, debit, credit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("借方または貸方");
    }

    @Test
    @DisplayName("借方と貸方の両方が null の場合は例外をスローする")
    void shouldThrowExceptionWhenBothAmountsNull() {
        AccountId accountId = AccountId.of(10);

        assertThatThrownBy(() -> JournalEntryLine.of(1, accountId, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("借方または貸方");
    }

    @Test
    @DisplayName("lineNumber が null の場合は例外をスローする")
    void shouldThrowExceptionWhenLineNumberIsNull() {
        AccountId accountId = AccountId.of(10);
        Money debit = Money.of(new BigDecimal("100"));

        assertThatThrownBy(() -> JournalEntryLine.of(null, accountId, debit, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("行番号");
    }

    @Test
    @DisplayName("accountId が null の場合は例外をスローする")
    void shouldThrowExceptionWhenAccountIdIsNull() {
        Money debit = Money.of(new BigDecimal("100"));

        assertThatThrownBy(() -> JournalEntryLine.of(1, null, debit, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("勘定科目 ID");
    }
}
