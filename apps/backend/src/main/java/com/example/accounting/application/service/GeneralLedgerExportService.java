package com.example.accounting.application.service;

import com.example.accounting.application.port.out.GetGeneralLedgerResult;
import com.example.accounting.application.port.out.GetGeneralLedgerResult.GeneralLedgerEntry;
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
public class GeneralLedgerExportService {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final String[] HEADERS = {"日付", "仕訳ID", "摘要", "借方", "貸方", "残高"};

    public Try<byte[]> exportToCsv(GetGeneralLedgerResult result) {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{
                buildAccountInfo(result), "", "", "", "", ""
        });

        for (GeneralLedgerEntry entry : result.content()) {
            rows.add(new String[]{
                    formatDate(entry),
                    valueOf(entry.journalEntryId()),
                    defaultString(entry.description()),
                    formatAmount(entry.debitAmount()),
                    formatAmount(entry.creditAmount()),
                    formatAmount(entry.runningBalance())
            });
        }

        rows.add(new String[]{
                buildTotalLine(result), "", "", "", "", ""
        });

        return CsvExportHelper.writeCsv(HEADERS, rows);
    }

    public Try<byte[]> exportToExcel(GetGeneralLedgerResult result) {
        return Try.of(() -> {
            try (Workbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("総勘定元帳");
                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle currencyStyle = createCurrencyStyle(workbook);

                int rowIdx = 0;
                Row accountInfoRow = sheet.createRow(rowIdx++);
                accountInfoRow.createCell(0).setCellValue(buildAccountInfo(result));

                Row headerRow = sheet.createRow(rowIdx++);
                for (int i = 0; i < HEADERS.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(HEADERS[i]);
                    cell.setCellStyle(headerStyle);
                }

                for (GeneralLedgerEntry entry : result.content()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(formatDate(entry));
                    row.createCell(1).setCellValue(entry.journalEntryId() == null ? 0 : entry.journalEntryId());
                    row.createCell(2).setCellValue(defaultString(entry.description()));

                    Cell debitCell = row.createCell(3);
                    debitCell.setCellValue(defaultAmount(entry.debitAmount()).doubleValue());
                    debitCell.setCellStyle(currencyStyle);

                    Cell creditCell = row.createCell(4);
                    creditCell.setCellValue(defaultAmount(entry.creditAmount()).doubleValue());
                    creditCell.setCellStyle(currencyStyle);

                    Cell balanceCell = row.createCell(5);
                    balanceCell.setCellValue(defaultAmount(entry.runningBalance()).doubleValue());
                    balanceCell.setCellStyle(currencyStyle);
                }

                Row totalRow = sheet.createRow(rowIdx);
                totalRow.createCell(0).setCellValue(buildTotalLine(result));

                for (int i = 0; i < HEADERS.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                workbook.write(out);
                return out.toByteArray();
            }
        });
    }

    private String formatDate(GeneralLedgerEntry entry) {
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

    private String buildAccountInfo(GetGeneralLedgerResult result) {
        return "科目コード: " + defaultString(result.accountCode()) + " 科目名: " + defaultString(result.accountName());
    }

    private String buildTotalLine(GetGeneralLedgerResult result) {
        return "借方合計: " + formatAmount(result.debitTotal())
                + " 貸方合計: " + formatAmount(result.creditTotal())
                + " 期末残高: " + formatAmount(result.closingBalance());
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
}
