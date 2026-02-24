package com.example.accounting.application.service;

import com.example.accounting.application.port.out.GetJournalEntriesResult;
import com.example.accounting.application.port.out.GetJournalEntriesResult.JournalEntrySummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JournalEntryExportService")
class JournalEntryExportServiceTest {

    private JournalEntryExportService service;

    @BeforeEach
    void setUp() {
        service = new JournalEntryExportService();
    }

    @Test
    @DisplayName("CSV をエクスポートできる")
    void shouldExportToCsv() {
        GetJournalEntriesResult result = new GetJournalEntriesResult(
                List.of(new JournalEntrySummary(
                        1,
                        LocalDate.of(2026, 1, 10),
                        "現金売上",
                        new BigDecimal("1000"),
                        new BigDecimal("1000"),
                        "POSTED",
                        1
                )),
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
        GetJournalEntriesResult result = new GetJournalEntriesResult(
                List.of(new JournalEntrySummary(
                        1,
                        LocalDate.of(2026, 1, 10),
                        "現金売上",
                        new BigDecimal("1000"),
                        new BigDecimal("1000"),
                        "POSTED",
                        1
                )),
                0,
                20,
                1,
                1
        );

        byte[] bytes = service.exportToExcel(result).get();

        assertThat(bytes).isNotEmpty();
    }
}
