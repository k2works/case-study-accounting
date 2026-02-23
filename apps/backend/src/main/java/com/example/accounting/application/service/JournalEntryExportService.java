package com.example.accounting.application.service;

import com.example.accounting.application.port.out.GetJournalEntriesResult;
import com.example.accounting.application.port.out.GetJournalEntriesResult.JournalEntrySummary;
import io.vavr.control.Try;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@SuppressWarnings({
        "PMD.AvoidMutableCollectionInstantiation",
        "PMD.CognitiveComplexity",
        "PMD.AvoidTraditionalForLoop"
})
public class JournalEntryExportService {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final String[] HEADERS = {"仕訳ID", "仕訳日", "摘要", "借方金額", "貸方金額", "ステータス"};

    public Try<byte[]> exportToCsv(GetJournalEntriesResult result) {
        List<String[]> rows = new ArrayList<>();
        for (JournalEntrySummary entry : result.content()) {
            rows.add(new String[]{
                    valueOf(entry.journalEntryId()),
                    formatDate(entry),
                    defaultString(entry.description()),
                    formatAmount(entry.totalDebitAmount()),
                    formatAmount(entry.totalCreditAmount()),
                    defaultString(entry.status())
            });
        }
        return CsvExportHelper.writeCsv(HEADERS, rows);
    }

    public Try<byte[]> exportToExcel(GetJournalEntriesResult result) {
        return Try.of(() -> {
            try (Workbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("仕訳一覧");
                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle currencyStyle = createCurrencyStyle(workbook);

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < HEADERS.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(HEADERS[i]);
                    cell.setCellStyle(headerStyle);
                }

                int rowIdx = 1;
                for (JournalEntrySummary entry : result.content()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(entry.journalEntryId() == null ? 0 : entry.journalEntryId());
                    row.createCell(1).setCellValue(formatDate(entry));
                    row.createCell(2).setCellValue(defaultString(entry.description()));

                    Cell debitCell = row.createCell(3);
                    debitCell.setCellValue(defaultAmount(entry.totalDebitAmount()).doubleValue());
                    debitCell.setCellStyle(currencyStyle);

                    Cell creditCell = row.createCell(4);
                    creditCell.setCellValue(defaultAmount(entry.totalCreditAmount()).doubleValue());
                    creditCell.setCellStyle(currencyStyle);

                    row.createCell(5).setCellValue(defaultString(entry.status()));
                }

                for (int i = 0; i < HEADERS.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                workbook.write(out);
                return out.toByteArray();
            }
        });
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        return style;
    }

    private String formatDate(JournalEntrySummary entry) {
        return entry.journalDate() == null ? "" : entry.journalDate().format(DATE_FMT);
    }

    private String formatAmount(BigDecimal amount) {
        return defaultAmount(amount).toPlainString();
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String valueOf(Integer value) {
        return value == null ? "" : String.valueOf(value);
    }
}
