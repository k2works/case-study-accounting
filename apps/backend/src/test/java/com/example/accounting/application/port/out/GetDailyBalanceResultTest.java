package com.example.accounting.application.port.out;

import com.example.accounting.application.port.out.GetDailyBalanceResult.DailyBalanceEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GetDailyBalanceResult")
class GetDailyBalanceResultTest {

    @Test
    @DisplayName("entries が null の場合は空のリストになる")
    void shouldDefaultEntriesToEmptyList() {
        GetDailyBalanceResult result = new GetDailyBalanceResult(
                1,
                "1101",
                "現金",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null
        );

        assertThat(result.entries()).isEmpty();
        assertThatThrownBy(() -> result.entries().add(buildEntry()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("entries はコピーされ、外部変更を受けない")
    void shouldCopyEntriesList() {
        List<DailyBalanceEntry> entries = new ArrayList<>();
        entries.add(buildEntry());

        GetDailyBalanceResult result = new GetDailyBalanceResult(
                1,
                "1101",
                "現金",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                entries
        );

        entries.clear();

        assertThat(result.entries()).hasSize(1);
        assertThatThrownBy(() -> result.entries().add(buildEntry()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private DailyBalanceEntry buildEntry() {
        return new DailyBalanceEntry(
                LocalDate.of(2024, 1, 1),
                BigDecimal.ONE,
                BigDecimal.ZERO,
                BigDecimal.ONE,
                1L
        );
    }
}
