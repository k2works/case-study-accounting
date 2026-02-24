package com.example.accounting.application.service;

import com.example.accounting.application.port.out.GetTrialBalanceResult;
import com.example.accounting.application.port.out.GetTrialBalanceResult.CategorySubtotal;
import com.example.accounting.application.port.out.GetTrialBalanceResult.TrialBalanceEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TrialBalanceExportService")
class TrialBalanceExportServiceTest {

    private TrialBalanceExportService service;

    @BeforeEach
    void setUp() {
        service = new TrialBalanceExportService();
    }

    @Test
    @DisplayName("CSV をエクスポートできる")
    void shouldExportToCsv() {
        byte[] bytes = service.exportToCsv(createResult()).get();

        assertThat(bytes).isNotEmpty();
    }

    @Test
    @DisplayName("Excel をエクスポートできる")
    void shouldExportToExcel() {
        byte[] bytes = service.exportToExcel(createResult()).get();

        assertThat(bytes).isNotEmpty();
    }

    @Test
    @DisplayName("PDF をエクスポートできる")
    void shouldExportToPdf() {
        byte[] bytes = service.exportToPdf(createResult()).get();

        assertThat(bytes).isNotEmpty();
        assertThat(new String(bytes, 0, 4)).isEqualTo("%PDF");
    }

    private GetTrialBalanceResult createResult() {
        List<TrialBalanceEntry> entries = List.of(
                new TrialBalanceEntry("101", "現金", "BS", "ASSET",
                        new BigDecimal("10000"), BigDecimal.ZERO),
                new TrialBalanceEntry("201", "買掛金", "BS", "LIABILITY",
                        BigDecimal.ZERO, new BigDecimal("7000")),
                new TrialBalanceEntry("301", "資本金", "BS", "EQUITY",
                        BigDecimal.ZERO, new BigDecimal("3000"))
        );

        List<CategorySubtotal> subtotals = List.of(
                new CategorySubtotal("ASSET", "資産", new BigDecimal("10000"), BigDecimal.ZERO),
                new CategorySubtotal("LIABILITY", "負債", BigDecimal.ZERO, new BigDecimal("7000")),
                new CategorySubtotal("EQUITY", "純資産", BigDecimal.ZERO, new BigDecimal("3000")),
                new CategorySubtotal("REVENUE", "収益", BigDecimal.ZERO, BigDecimal.ZERO),
                new CategorySubtotal("EXPENSE", "費用", BigDecimal.ZERO, BigDecimal.ZERO)
        );

        return new GetTrialBalanceResult(
                LocalDate.of(2026, 1, 31),
                new BigDecimal("10000"),
                new BigDecimal("10000"),
                true,
                BigDecimal.ZERO,
                entries,
                subtotals
        );
    }
}
