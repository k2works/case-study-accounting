package com.example.accounting.application.service;

import com.example.accounting.application.port.out.GetProfitAndLossResult;
import com.example.accounting.application.port.out.GetProfitAndLossResult.ProfitAndLossEntry;
import com.example.accounting.application.port.out.GetProfitAndLossResult.ProfitAndLossSection;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProfitAndLossExportService")
class ProfitAndLossExportServiceTest {

    private ProfitAndLossExportService service;

    @BeforeEach
    void setUp() {
        service = new ProfitAndLossExportService();
    }

    private GetProfitAndLossResult createTestResult(LocalDate dateFrom, LocalDate dateTo) {
        var entry1 = new ProfitAndLossEntry("4000", "売上高", "REVENUE",
                new BigDecimal("500000"), null);
        var entry2 = new ProfitAndLossEntry("5000", "給料", "EXPENSE",
                new BigDecimal("300000"), null);
        var revenueSection = new ProfitAndLossSection("REVENUE", "収益の部",
                List.of(entry1), new BigDecimal("500000"), null);
        var expenseSection = new ProfitAndLossSection("EXPENSE", "費用の部",
                List.of(entry2), new BigDecimal("300000"), null);
        return new GetProfitAndLossResult(dateFrom, dateTo, null, null,
                List.of(revenueSection, expenseSection),
                new BigDecimal("500000"), new BigDecimal("300000"),
                new BigDecimal("200000"));
    }

    @Nested
    @DisplayName("Excel エクスポート")
    class ExportToExcel {

        @Test
        @DisplayName("全セクションを含む結果を XLSX 形式でエクスポートできる")
        void shouldExportFullResultToExcel() throws IOException {
            GetProfitAndLossResult result = createTestResult(
                    LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31));

            byte[] bytes = service.exportToExcel(result).get();

            assertThat(bytes).isNotEmpty();
            try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
                assertThat(workbook.getSheetAt(0).getSheetName()).isEqualTo("損益計算書");
            }
        }

        @Test
        @DisplayName("日付が null でも例外が発生しない")
        void shouldExportWithNullDates() throws IOException {
            GetProfitAndLossResult result = createTestResult(null, null);

            byte[] bytes = service.exportToExcel(result).get();

            assertThat(bytes).isNotEmpty();
            try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
                assertThat(workbook.getSheetAt(0)).isNotNull();
            }
        }

        @Test
        @DisplayName("セクションが空でも例外が発生しない")
        void shouldExportWithEmptySections() {
            GetProfitAndLossResult result = new GetProfitAndLossResult(
                    LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31),
                    null, null,
                    List.of(),
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

            byte[] bytes = service.exportToExcel(result).get();

            assertThat(bytes).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("CSV エクスポート")
    class ExportToCsv {

        @Test
        @DisplayName("全セクションを含む結果を CSV 形式でエクスポートできる")
        void shouldExportFullResultToCsv() {
            GetProfitAndLossResult result = createTestResult(
                    LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31));

            byte[] bytes = service.exportToCsv(result).get();

            assertThat(bytes).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("PDF エクスポート")
    class ExportToPdf {

        @Test
        @DisplayName("全セクションを含む結果を PDF 形式でエクスポートできる")
        void shouldExportFullResultToPdf() {
            GetProfitAndLossResult result = createTestResult(
                    LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31));

            byte[] bytes = service.exportToPdf(result).get();

            assertThat(bytes).isNotEmpty();
            assertThat(new String(bytes, 0, 4)).isEqualTo("%PDF");
        }

        @Test
        @DisplayName("日付が null でも例外が発生しない")
        void shouldExportWithNullDates() {
            GetProfitAndLossResult result = createTestResult(null, null);

            byte[] bytes = service.exportToPdf(result).get();

            assertThat(bytes).isNotEmpty();
            assertThat(new String(bytes, 0, 4)).isEqualTo("%PDF");
        }

        @Test
        @DisplayName("セクションが空でも例外が発生しない")
        void shouldExportWithEmptySections() {
            GetProfitAndLossResult result = new GetProfitAndLossResult(
                    LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31),
                    null, null,
                    List.of(),
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

            byte[] bytes = service.exportToPdf(result).get();

            assertThat(bytes).isNotEmpty();
            assertThat(new String(bytes, 0, 4)).isEqualTo("%PDF");
        }

        @Test
        @DisplayName("複数セクション（REVENUE, EXPENSE）を含む結果をエクスポートできる")
        void shouldExportWithMultipleSections() {
            GetProfitAndLossResult result = createTestResult(
                    LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31));

            byte[] bytes = service.exportToPdf(result).get();

            assertThat(bytes).isNotEmpty();
            assertThat(bytes.length).isGreaterThan(100);
            assertThat(new String(bytes, 0, 4)).isEqualTo("%PDF");
        }
    }
}
