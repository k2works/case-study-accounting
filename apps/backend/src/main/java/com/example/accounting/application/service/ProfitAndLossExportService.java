package com.example.accounting.application.service;

import com.example.accounting.application.port.out.GetProfitAndLossResult;
import com.example.accounting.application.port.out.GetProfitAndLossResult.ProfitAndLossEntry;
import com.example.accounting.application.port.out.GetProfitAndLossResult.ProfitAndLossSection;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import io.vavr.control.Try;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@SuppressWarnings("PMD.AvoidMutableCollectionInstantiation") // OpenPDF の Paragraph/Phrase は new 必須
public class ProfitAndLossExportService {

    private static final String FONT_PATH = "/fonts/NotoSansJP-Regular.ttf";
    private static final Color HEADER_BG = new Color(240, 240, 240);

    public Try<byte[]> exportToExcel(GetProfitAndLossResult result) {
        return Try.of(() -> {
            try (Workbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("損益計算書");
                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle currencyStyle = createCurrencyStyle(workbook);

                int currentRow = writeExcelTitle(sheet, result, 0);
                currentRow++;

                for (ProfitAndLossSection section : result.sections()) {
                    currentRow = writeExcelSection(sheet, section, currentRow, headerStyle, currencyStyle);
                    currentRow++;
                }

                currentRow++;
                writeExcelTotals(sheet, result, currentRow, headerStyle, currencyStyle);

                sheet.autoSizeColumn(0);
                sheet.autoSizeColumn(1);

                workbook.write(out);
                return out.toByteArray();
            }
        });
    }

    public Try<byte[]> exportToPdf(GetProfitAndLossResult result) {
        return Try.of(() -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (Document document = new Document(PageSize.A4)) {
                PdfWriter.getInstance(document, out);
                document.open();

                Font titleFont = createJapaneseFont(16, Font.BOLD);
                Font headerFont = createJapaneseFont(10, Font.BOLD);
                Font bodyFont = createJapaneseFont(9, Font.NORMAL);

                writePdfTitle(document, result, titleFont, bodyFont);

                PdfPTable table = new PdfPTable(new float[]{60f, 40f});
                table.setWidthPercentage(100);

                for (ProfitAndLossSection section : result.sections()) {
                    writePdfSection(table, section, headerFont, bodyFont);
                }

                addPdfTotalRows(table, result, headerFont);

                document.add(table);
            }
            return out.toByteArray();
        });
    }

    private int writeExcelTitle(Sheet sheet, GetProfitAndLossResult result, int startRow) {
        Row titleRow = sheet.createRow(startRow);
        titleRow.createCell(0).setCellValue("損益計算書");
        if (result.dateFrom() != null || result.dateTo() != null) {
            String period = buildPeriodString(result);
            titleRow.createCell(1).setCellValue(period);
        }
        return startRow + 1;
    }

    private int writeExcelSection(Sheet sheet, ProfitAndLossSection section, int startRow,
                                   CellStyle headerStyle, CellStyle currencyStyle) {
        int currentRow = startRow;
        Row sectionRow = sheet.createRow(currentRow++);
        sectionRow.createCell(0).setCellValue(section.sectionDisplayName());
        sectionRow.getCell(0).setCellStyle(headerStyle);

        for (ProfitAndLossEntry entry : section.entries()) {
            Row entryRow = sheet.createRow(currentRow++);
            entryRow.createCell(0).setCellValue("  " + entry.accountCode() + " " + entry.accountName());
            entryRow.createCell(1).setCellValue(entry.amount().doubleValue());
            entryRow.getCell(1).setCellStyle(currencyStyle);
        }

        Row subtotalRow = sheet.createRow(currentRow++);
        subtotalRow.createCell(0).setCellValue(section.sectionDisplayName() + "合計");
        subtotalRow.getCell(0).setCellStyle(headerStyle);
        subtotalRow.createCell(1).setCellValue(section.subtotal().doubleValue());
        subtotalRow.getCell(1).setCellStyle(currencyStyle);

        return currentRow;
    }

    private void writeExcelTotals(Sheet sheet, GetProfitAndLossResult result, int startRow,
                                   CellStyle headerStyle, CellStyle currencyStyle) {
        Row revenueRow = sheet.createRow(startRow);
        revenueRow.createCell(0).setCellValue("収益合計");
        revenueRow.getCell(0).setCellStyle(headerStyle);
        revenueRow.createCell(1).setCellValue(result.totalRevenue().doubleValue());
        revenueRow.getCell(1).setCellStyle(currencyStyle);

        Row expenseRow = sheet.createRow(startRow + 1);
        expenseRow.createCell(0).setCellValue("費用合計");
        expenseRow.getCell(0).setCellStyle(headerStyle);
        expenseRow.createCell(1).setCellValue(result.totalExpense().doubleValue());
        expenseRow.getCell(1).setCellStyle(currencyStyle);

        Row netIncomeRow = sheet.createRow(startRow + 2);
        netIncomeRow.createCell(0).setCellValue("当期純利益");
        netIncomeRow.getCell(0).setCellStyle(headerStyle);
        netIncomeRow.createCell(1).setCellValue(result.netIncome().doubleValue());
        netIncomeRow.getCell(1).setCellStyle(currencyStyle);
    }

    private void writePdfTitle(Document document, GetProfitAndLossResult result,
                                Font titleFont, Font bodyFont) {
        Paragraph title = new Paragraph("損益計算書", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        if (result.dateFrom() != null || result.dateTo() != null) {
            String period = buildPeriodString(result);
            Paragraph datePara = new Paragraph(period, bodyFont);
            datePara.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(datePara);
        }
        document.add(new Paragraph(" "));
    }

    private void writePdfSection(PdfPTable table, ProfitAndLossSection section,
                                  Font headerFont, Font bodyFont) {
        addPdfCell(table, section.sectionDisplayName(), headerFont, true);
        addPdfCell(table, "", headerFont, true);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.JAPAN);
        for (ProfitAndLossEntry entry : section.entries()) {
            addPdfCell(table, "  " + entry.accountCode() + " " + entry.accountName(), bodyFont, false);
            addPdfCell(table, nf.format(entry.amount()), bodyFont, false);
        }

        addPdfCell(table, section.sectionDisplayName() + "合計", headerFont, true);
        addPdfCell(table, nf.format(section.subtotal()), headerFont, true);
    }

    private void addPdfTotalRows(PdfPTable table, GetProfitAndLossResult result, Font font) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.JAPAN);

        addPdfCell(table, "", font, false);
        addPdfCell(table, "", font, false);

        addPdfCell(table, "収益合計", font, true);
        addPdfCell(table, nf.format(result.totalRevenue()), font, true);

        addPdfCell(table, "費用合計", font, true);
        addPdfCell(table, nf.format(result.totalExpense()), font, true);

        addPdfCell(table, "当期純利益", font, true);
        addPdfCell(table, nf.format(result.netIncome()), font, true);
    }

    private void addPdfCell(PdfPTable table, String text, Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        if (isHeader) {
            cell.setBackgroundColor(HEADER_BG);
        }
        cell.setPadding(4);
        table.addCell(cell);
    }

    private String buildPeriodString(GetProfitAndLossResult result) {
        if (result.dateFrom() != null && result.dateTo() != null) {
            return "期間: " + result.dateFrom() + " ～ " + result.dateTo();
        } else if (result.dateFrom() != null) {
            return "開始日: " + result.dateFrom();
        } else if (result.dateTo() != null) {
            return "終了日: " + result.dateTo();
        }
        return "";
    }

    private Font createJapaneseFont(int size, int style) {
        try (InputStream fontStream = getClass().getResourceAsStream(FONT_PATH)) {
            if (fontStream == null) {
                return new Font(Font.HELVETICA, size, style);
            }
            byte[] fontBytes = fontStream.readAllBytes();
            BaseFont baseFont = BaseFont.createFont(
                    "NotoSansJP-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    true, fontBytes, null);
            return new Font(baseFont, size, style);
        } catch (IOException | com.lowagie.text.DocumentException e) {
            return new Font(Font.HELVETICA, size, style);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }
}
