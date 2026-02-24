package com.example.accounting.application.service;

import com.example.accounting.application.port.out.GetTrialBalanceResult;
import com.example.accounting.application.port.out.GetTrialBalanceResult.CategorySubtotal;
import com.example.accounting.application.port.out.GetTrialBalanceResult.TrialBalanceEntry;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import io.vavr.control.Try;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@SuppressWarnings({
        "PMD.AvoidMutableCollectionInstantiation",
        "PMD.CognitiveComplexity",
        "PMD.AvoidTraditionalForLoop"
}) // OpenPDF の Paragraph/Phrase は new 必須
public class TrialBalanceExportService extends AbstractStatementExportService {

    private static final String[] HEADERS = {"科目コード", "科目名", "借方残高", "貸方残高"};

    public Try<byte[]> exportToCsv(GetTrialBalanceResult result) {
        List<String[]> rows = new ArrayList<>();

        for (TrialBalanceEntry entry : result.entries()) {
            rows.add(new String[]{
                    defaultString(entry.accountCode()),
                    defaultString(entry.accountName()),
                    formatAmount(entry.debitBalance()),
                    formatAmount(entry.creditBalance())
            });
        }

        for (CategorySubtotal subtotal : result.categorySubtotals()) {
            rows.add(new String[]{
                    "",
                    defaultString(subtotal.accountTypeDisplayName()) + "小計",
                    formatAmount(subtotal.debitSubtotal()),
                    formatAmount(subtotal.creditSubtotal())
            });
        }

        rows.add(new String[]{
                "",
                "合計",
                formatAmount(result.totalDebit()),
                formatAmount(result.totalCredit())
        });

        return CsvExportHelper.writeCsv(HEADERS, rows);
    }

    public Try<byte[]> exportToExcel(GetTrialBalanceResult result) {
        return Try.of(() -> {
            try (Workbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("残高試算表");
                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle currencyStyle = createCurrencyStyle(workbook);

                int rowIdx = 0;
                Row headerRow = sheet.createRow(rowIdx++);
                for (int i = 0; i < HEADERS.length; i++) {
                    headerRow.createCell(i).setCellValue(HEADERS[i]);
                    headerRow.getCell(i).setCellStyle(headerStyle);
                }

                for (TrialBalanceEntry entry : result.entries()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(defaultString(entry.accountCode()));
                    row.createCell(1).setCellValue(defaultString(entry.accountName()));

                    row.createCell(2).setCellValue(defaultAmount(entry.debitBalance()).doubleValue());
                    row.getCell(2).setCellStyle(currencyStyle);

                    row.createCell(3).setCellValue(defaultAmount(entry.creditBalance()).doubleValue());
                    row.getCell(3).setCellStyle(currencyStyle);
                }

                for (CategorySubtotal subtotal : result.categorySubtotals()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(1).setCellValue(defaultString(subtotal.accountTypeDisplayName()) + "小計");
                    row.getCell(1).setCellStyle(headerStyle);

                    row.createCell(2).setCellValue(defaultAmount(subtotal.debitSubtotal()).doubleValue());
                    row.getCell(2).setCellStyle(currencyStyle);

                    row.createCell(3).setCellValue(defaultAmount(subtotal.creditSubtotal()).doubleValue());
                    row.getCell(3).setCellStyle(currencyStyle);
                }

                Row totalRow = sheet.createRow(rowIdx);
                totalRow.createCell(1).setCellValue("合計");
                totalRow.getCell(1).setCellStyle(headerStyle);

                totalRow.createCell(2).setCellValue(defaultAmount(result.totalDebit()).doubleValue());
                totalRow.getCell(2).setCellStyle(currencyStyle);

                totalRow.createCell(3).setCellValue(defaultAmount(result.totalCredit()).doubleValue());
                totalRow.getCell(3).setCellStyle(currencyStyle);

                for (int i = 0; i < HEADERS.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                workbook.write(out);
                return out.toByteArray();
            }
        });
    }

    public Try<byte[]> exportToPdf(GetTrialBalanceResult result) {
        return Try.of(() -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (Document document = new Document(PageSize.A4)) {
                PdfWriter.getInstance(document, out);
                document.open();

                Font titleFont = createJapaneseFont(16, Font.BOLD);
                Font headerFont = createJapaneseFont(10, Font.BOLD);
                Font bodyFont = createJapaneseFont(9, Font.NORMAL);

                Paragraph title = new Paragraph("残高試算表", titleFont);
                title.setAlignment(Paragraph.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph(" "));

                PdfPTable table = new PdfPTable(new float[]{20f, 40f, 20f, 20f});
                table.setWidthPercentage(100);

                addPdfCell(table, HEADERS[0], headerFont, true);
                addPdfCell(table, HEADERS[1], headerFont, true);
                addPdfCell(table, HEADERS[2], headerFont, true);
                addPdfCell(table, HEADERS[3], headerFont, true);

                for (TrialBalanceEntry entry : result.entries()) {
                    addPdfCell(table, defaultString(entry.accountCode()), bodyFont, false);
                    addPdfCell(table, defaultString(entry.accountName()), bodyFont, false);
                    addPdfCell(table, formatAmount(entry.debitBalance()), bodyFont, false);
                    addPdfCell(table, formatAmount(entry.creditBalance()), bodyFont, false);
                }

                for (CategorySubtotal subtotal : result.categorySubtotals()) {
                    addPdfCell(table, "", headerFont, true);
                    addPdfCell(table, defaultString(subtotal.accountTypeDisplayName()) + "小計", headerFont, true);
                    addPdfCell(table, formatAmount(subtotal.debitSubtotal()), headerFont, true);
                    addPdfCell(table, formatAmount(subtotal.creditSubtotal()), headerFont, true);
                }

                addPdfCell(table, "", headerFont, true);
                addPdfCell(table, "合計", headerFont, true);
                addPdfCell(table, formatAmount(result.totalDebit()), headerFont, true);
                addPdfCell(table, formatAmount(result.totalCredit()), headerFont, true);

                document.add(table);
            }
            return out.toByteArray();
        });
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
}
