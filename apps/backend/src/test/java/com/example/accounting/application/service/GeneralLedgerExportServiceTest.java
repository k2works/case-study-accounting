package com.example.accounting.application.service;

import com.example.accounting.application.port.out.GetGeneralLedgerResult;
import com.example.accounting.application.port.out.GetGeneralLedgerResult.GeneralLedgerEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GeneralLedgerExportService")
class GeneralLedgerExportServiceTest {

    private GeneralLedgerExportService service;

    @BeforeEach
    void setUp() {
        service = new GeneralLedgerExportService();
    }

    @Test
    @DisplayName("CSV をエクスポートできる")
    void shouldExportToCsv() {
        GetGeneralLedgerResult result = new GetGeneralLedgerResult(
                List.of(new GeneralLedgerEntry(
                        10,
                        LocalDate.of(2026, 1, 15),
                        "売上計上",
                        new BigDecimal("5000"),
                        BigDecimal.ZERO,
                        new BigDecimal("5000")
                )),
                100,
                "101",
                "現金",
                BigDecimal.ZERO,
                new BigDecimal("5000"),
                BigDecimal.ZERO,
                new BigDecimal("5000"),
                0,
                20,
                1,
                1
        );

        byte[] bytes = service.exportToCsv(result).get();

        assertThat(bytes).isNotEmpty();
    }

    @Test
    @DisplayName("Excel をエクスポートできる")
    void shouldExportToExcel() {
        GetGeneralLedgerResult result = new GetGeneralLedgerResult(
                List.of(new GeneralLedgerEntry(
                        10,
                        LocalDate.of(2026, 1, 15),
                        "売上計上",
                        new BigDecimal("5000"),
                        BigDecimal.ZERO,
                        new BigDecimal("5000")
                )),
                100,
                "101",
                "現金",
                BigDecimal.ZERO,
                new BigDecimal("5000"),
                BigDecimal.ZERO,
                new BigDecimal("5000"),
                0,
                20,
                1,
                1
        );

        byte[] bytes = service.exportToExcel(result).get();

        assertThat(bytes).isNotEmpty();
    }
}
