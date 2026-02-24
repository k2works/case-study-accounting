package com.example.accounting.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CsvExportHelper")
class CsvExportHelperTest {

    @Test
    @DisplayName("BOM 付き UTF-8 で CSV を出力できる")
    void shouldWriteCsvWithUtf8Bom() {
        byte[] bytes = CsvExportHelper.writeCsv(
                new String[]{"列1", "列2"},
                List.<String[]>of(new String[]{"a", "b"})
        ).get();

        assertThat(bytes).isNotEmpty();
        assertThat(bytes[0]).isEqualTo((byte) 0xEF);
        assertThat(bytes[1]).isEqualTo((byte) 0xBB);
        assertThat(bytes[2]).isEqualTo((byte) 0xBF);

        String csv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(csv).startsWith("\uFEFF列1,列2\r\n");
    }

    @Test
    @DisplayName("CSV エスケープ処理を適用できる")
    void shouldEscapeCsvFields() {
        byte[] bytes = CsvExportHelper.writeCsv(
                new String[]{"A", "B", "C", "D"},
                List.<String[]>of(new String[]{"a,b", "c\"d", "e\nf", null})
        ).get();

        String csv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(csv).contains("\"a,b\"");
        assertThat(csv).contains("\"c\"\"d\"");
        assertThat(csv).contains("\"e\nf\"");
        assertThat(csv).contains(",\r\n");
    }

    @Test
    @DisplayName("データ行が空でもヘッダーのみ出力できる")
    void shouldWriteHeaderOnlyWhenRowsAreEmpty() {
        byte[] bytes = CsvExportHelper.writeCsv(
                new String[]{"列1", "列2"},
                List.of()
        ).get();

        String csv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(csv).isEqualTo("\uFEFF列1,列2\r\n");
    }
}
