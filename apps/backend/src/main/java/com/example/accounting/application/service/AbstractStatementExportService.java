package com.example.accounting.application.service;

import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * 財務諸表エクスポートの共通処理基底クラス
 */
@SuppressWarnings({"PMD.AvoidMutableCollectionInstantiation", "PMD.AbstractClassWithoutAbstractMethod"})
abstract class AbstractStatementExportService {

    protected static final String FONT_PATH = "/fonts/NotoSansJP-Regular.ttf";
    protected static final Color HEADER_BG = new Color(240, 240, 240);

    protected record SectionExcelData(
            String displayName,
            List<EntryExcelData> entries,
            BigDecimal subtotal
    ) {
        protected SectionExcelData {
            entries = entries == null ? List.of() : List.copyOf(entries);
        }
    }

    protected record EntryExcelData(String accountCode, String accountName, BigDecimal amount) { }

    protected int writeExcelSection(Sheet sheet, SectionExcelData section, int startRow,
                                     CellStyle headerStyle, CellStyle currencyStyle) {
        int currentRow = startRow;
        Row sectionRow = sheet.createRow(currentRow++);
        sectionRow.createCell(0).setCellValue(section.displayName());
        sectionRow.getCell(0).setCellStyle(headerStyle);

        for (EntryExcelData entry : section.entries()) {
            Row entryRow = sheet.createRow(currentRow++);
            entryRow.createCell(0).setCellValue("  " + entry.accountCode() + " " + entry.accountName());
            entryRow.createCell(1).setCellValue(entry.amount().doubleValue());
            entryRow.getCell(1).setCellStyle(currencyStyle);
        }

        Row subtotalRow = sheet.createRow(currentRow++);
        subtotalRow.createCell(0).setCellValue(section.displayName() + "合計");
        subtotalRow.getCell(0).setCellStyle(headerStyle);
        subtotalRow.createCell(1).setCellValue(section.subtotal().doubleValue());
        subtotalRow.getCell(1).setCellStyle(currencyStyle);

        return currentRow;
    }

    protected void addPdfCell(PdfPTable table, String text, Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        if (isHeader) {
            cell.setBackgroundColor(HEADER_BG);
        }
        cell.setPadding(4);
        table.addCell(cell);
    }

    protected Font createJapaneseFont(int size, int style) {
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

    protected CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    protected CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }
}
