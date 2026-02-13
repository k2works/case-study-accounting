package com.example.accounting.application.port.out;

import com.example.accounting.application.port.out.GetMonthlyBalanceResult.MonthlyBalanceEntry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetMonthlyBalanceResultTest {

    @Test
    void shouldCreateWithEntries() {
        MonthlyBalanceEntry entry = new MonthlyBalanceEntry(
                1, new BigDecimal("10000"), new BigDecimal("5000"),
                new BigDecimal("3000"), new BigDecimal("12000")
        );
        GetMonthlyBalanceResult result = new GetMonthlyBalanceResult(
                "1100", "現金", 2024,
                new BigDecimal("10000"), new BigDecimal("5000"),
                new BigDecimal("3000"), new BigDecimal("12000"),
                List.of(entry)
        );

        assertThat(result.accountCode()).isEqualTo("1100");
        assertThat(result.accountName()).isEqualTo("現金");
        assertThat(result.fiscalPeriod()).isEqualTo(2024);
        assertThat(result.entries()).hasSize(1);
        assertThat(result.entries().getFirst().month()).isEqualTo(1);
    }

    @Test
    void shouldDefaultToEmptyListWhenEntriesIsNull() {
        GetMonthlyBalanceResult result = new GetMonthlyBalanceResult(
                "1100", "現金", 2024,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null
        );

        assertThat(result.entries()).isEmpty();
    }

    @Test
    void shouldReturnImmutableEntryList() {
        MonthlyBalanceEntry entry = new MonthlyBalanceEntry(
                1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );
        GetMonthlyBalanceResult result = new GetMonthlyBalanceResult(
                "1100", "現金", 2024,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                List.of(entry)
        );

        assertThat(result.entries()).isUnmodifiable();
    }
}
