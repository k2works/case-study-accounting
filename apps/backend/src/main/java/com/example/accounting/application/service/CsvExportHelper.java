package com.example.accounting.application.service;

import io.vavr.control.Try;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * CSV エクスポート共通ヘルパー。
 * BOM 付き UTF-8 で出力（Excel での日本語文字化け防止）。
 */
public final class CsvExportHelper {
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private CsvExportHelper() {
    }

    public static Try<byte[]> writeCsv(String[] headers, List<String[]> rows) {
        return Try.of(() -> {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                 OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                out.write(UTF8_BOM);
                writeLine(writer, headers);
                for (String[] row : rows) {
                    writeLine(writer, row);
                }
                writer.flush();
                return out.toByteArray();
            }
        });
    }

    @SuppressWarnings({"PMD.UseVarargs", "PMD.AvoidTraditionalForLoop", "PMD.AvoidCheckedExceptionDeclaration"})
    private static void writeLine(OutputStreamWriter writer, String[] fields) throws IOException {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                writer.write(',');
            }
            writer.write(escapeCsv(fields[i]));
        }
        writer.write("\r\n");
    }

    private static String escapeCsv(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
