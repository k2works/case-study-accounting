package com.example.accounting.application.port.out;

import com.example.accounting.application.port.out.GetTrialBalanceResult.CategorySubtotal;
import com.example.accounting.application.port.out.GetTrialBalanceResult.TrialBalanceEntry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetTrialBalanceResultTest {

    @Test
    void shouldCreateWithAllFields() {
        LocalDate date = LocalDate.of(2024, 6, 30);
        TrialBalanceEntry entry = new TrialBalanceEntry(
                "1000", "現金", "BS", "ASSET",
                new BigDecimal("50000"), BigDecimal.ZERO
        );
        CategorySubtotal subtotal = new CategorySubtotal(
                "ASSET", "資産",
                new BigDecimal("50000"), BigDecimal.ZERO
        );
        GetTrialBalanceResult result = new GetTrialBalanceResult(
                date, new BigDecimal("50000"), BigDecimal.ZERO,
                false, new BigDecimal("50000"),
                List.of(entry), List.of(subtotal)
        );

        assertThat(result.date()).isEqualTo(date);
        assertThat(result.totalDebit()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(result.balanced()).isFalse();
        assertThat(result.entries()).hasSize(1);
        assertThat(result.categorySubtotals()).hasSize(1);
    }

    @Test
    void shouldDefaultToEmptyListsWhenNull() {
        GetTrialBalanceResult result = new GetTrialBalanceResult(
                null, BigDecimal.ZERO, BigDecimal.ZERO,
                true, BigDecimal.ZERO,
                null, null
        );

        assertThat(result.entries()).isEmpty();
        assertThat(result.categorySubtotals()).isEmpty();
    }

    @Test
    void shouldReturnImmutableLists() {
        TrialBalanceEntry entry = new TrialBalanceEntry(
                "1000", "現金", "BS", "ASSET", BigDecimal.ZERO, BigDecimal.ZERO
        );
        CategorySubtotal subtotal = new CategorySubtotal(
                "ASSET", "資産", BigDecimal.ZERO, BigDecimal.ZERO
        );
        GetTrialBalanceResult result = new GetTrialBalanceResult(
                null, BigDecimal.ZERO, BigDecimal.ZERO,
                true, BigDecimal.ZERO,
                List.of(entry), List.of(subtotal)
        );

        assertThat(result.entries()).isUnmodifiable();
        assertThat(result.categorySubtotals()).isUnmodifiable();
    }
}
