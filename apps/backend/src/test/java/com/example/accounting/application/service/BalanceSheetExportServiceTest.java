package com.example.accounting.application.service;

import com.example.accounting.application.port.out.GetBalanceSheetResult;
import com.example.accounting.application.port.out.GetBalanceSheetResult.BalanceSheetEntry;
import com.example.accounting.application.port.out.GetBalanceSheetResult.BalanceSheetSection;
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

@DisplayName("BalanceSheetExportService")
class BalanceSheetExportServiceTest {

    private BalanceSheetExportService service;

    @BeforeEach
    void setUp() {
        service = new BalanceSheetExportService();
    }

    private GetBalanceSheetResult createTestResult(LocalDate date) {
        var entry1 = new BalanceSheetEntry("100", "現金", "ASSET", new BigDecimal("500000"), null);
        var entry2 = new BalanceSheetEntry("200", "買掛金", "LIABILITY", new BigDecimal("200000"), null);
        var entry3 = new BalanceSheetEntry("300", "資本金", "EQUITY", new BigDecimal("300000"), null);
        var assetSection = new BalanceSheetSection("ASSET", "資産の部", List.of(entry1),
                new BigDecimal("500000"), null);
        var liabilitySection = new BalanceSheetSection("LIABILITY", "負債の部", List.of(entry2),
                new BigDecimal("200000"), null);
        var equitySection = new BalanceSheetSection("EQUITY", "純資産の部", List.of(entry3),
                new BigDecimal("300000"), null);
        return new GetBalanceSheetResult(date, null,
                List.of(assetSection, liabilitySection, equitySection),
                new BigDecimal("500000"), new BigDecimal("200000"),
                new BigDecimal("300000"), new BigDecimal("500000"),
                true, BigDecimal.ZERO);
    }

    @Nested
    @DisplayName("Excel エクスポート")
    class ExportToExcel {

        @Test
        @DisplayName("全セクションを含む結果を XLSX 形式でエクスポートできる")
        void shouldExportFullResultToExcel() throws IOException {
            GetBalanceSheetResult result = createTestResult(LocalDate.of(2026, 3, 31));

            byte[] bytes = service.exportToExcel(result);

            assertThat(bytes).isNotEmpty();
            // 有効な XLSX ファイルとして読み込めることを検証
            try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
                assertThat(workbook.getSheetAt(0).getSheetName()).isEqualTo("貸借対照表");
            }
        }

        @Test
        @DisplayName("date が null でも例外が発生しない")
        void shouldExportWithNullDate() throws IOException {
            GetBalanceSheetResult result = createTestResult(null);

            byte[] bytes = service.exportToExcel(result);

            assertThat(bytes).isNotEmpty();
            try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
                assertThat(workbook.getSheetAt(0)).isNotNull();
            }
        }

        @Test
        @DisplayName("セクションが空でも例外が発生しない")
        void shouldExportWithEmptySections() throws IOException {
            GetBalanceSheetResult result = new GetBalanceSheetResult(
                    LocalDate.of(2026, 3, 31), null,
                    List.of(),
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    true, BigDecimal.ZERO);

            byte[] bytes = service.exportToExcel(result);

            assertThat(bytes).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("PDF エクスポート")
    class ExportToPdf {

        @Test
        @DisplayName("全セクションを含む結果を PDF 形式でエクスポートできる")
        void shouldExportFullResultToPdf() throws IOException {
            GetBalanceSheetResult result = createTestResult(LocalDate.of(2026, 3, 31));

            byte[] bytes = service.exportToPdf(result);

            assertThat(bytes).isNotEmpty();
            // PDF ファイルは %PDF で始まる
            assertThat(new String(bytes, 0, 4)).isEqualTo("%PDF");
        }

        @Test
        @DisplayName("date が null でも例外が発生しない")
        void shouldExportWithNullDate() throws IOException {
            GetBalanceSheetResult result = createTestResult(null);

            byte[] bytes = service.exportToPdf(result);

            assertThat(bytes).isNotEmpty();
            assertThat(new String(bytes, 0, 4)).isEqualTo("%PDF");
        }

        @Test
        @DisplayName("セクションが空でも例外が発生しない")
        void shouldExportWithEmptySections() throws IOException {
            GetBalanceSheetResult result = new GetBalanceSheetResult(
                    LocalDate.of(2026, 3, 31), null,
                    List.of(),
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    true, BigDecimal.ZERO);

            byte[] bytes = service.exportToPdf(result);

            assertThat(bytes).isNotEmpty();
            assertThat(new String(bytes, 0, 4)).isEqualTo("%PDF");
        }

        @Test
        @DisplayName("複数セクション（ASSET, LIABILITY, EQUITY）を含む結果をエクスポートできる")
        void shouldExportWithMultipleSections() throws IOException {
            GetBalanceSheetResult result = createTestResult(LocalDate.of(2026, 3, 31));

            byte[] bytes = service.exportToPdf(result);

            assertThat(bytes).isNotEmpty();
            assertThat(bytes.length).isGreaterThan(100);
            assertThat(new String(bytes, 0, 4)).isEqualTo("%PDF");
        }
    }
}
