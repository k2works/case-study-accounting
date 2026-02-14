package com.example.accounting.application.service;

import com.example.accounting.application.port.out.GetBalanceSheetResult;
import com.example.accounting.application.port.out.GetBalanceSheetResult.BalanceSheetEntry;
import com.example.accounting.application.port.out.GetBalanceSheetResult.BalanceSheetSection;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class BalanceSheetExportService {

    private static final String FONT_PATH = "/fonts/NotoSansJP-Regular.ttf";
    private static final Color HEADER_BG = new Color(240, 240, 240);
    private static final String[] EMPTY_ROW = {"", ""};

    public byte[] exportToExcel(GetBalanceSheetResult result) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("貸借対照表");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            int currentRow = writeExcelTitle(sheet, result, 0);
            currentRow++;

            for (BalanceSheetSection section : result.sections()) {
                currentRow = writeExcelSection(sheet, section, currentRow, headerStyle, currencyStyle);
                currentRow++;
            }

            currentRow++;
            writeExcelTotals(sheet, result, currentRow, currencyStyle);

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportToPdf(GetBalanceSheetResult result) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document document = new Document(PageSize.A4.rotate())) {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = createJapaneseFont(16, Font.BOLD);
            Font headerFont = createJapaneseFont(10, Font.BOLD);
            Font bodyFont = createJapaneseFont(9, Font.NORMAL);

            writePdfTitle(document, result, titleFont, bodyFont);

            PdfPTable table = new PdfPTable(new float[]{15f, 35f, 15f, 35f});
            table.setWidthPercentage(100);

            addPdfHeaderRow(table, headerFont);
            writePdfSections(table, result, bodyFont);
            addPdfTotalRow(table, result, headerFont);

            document.add(table);
        }
        return out.toByteArray();
    }

    private int writeExcelTitle(Sheet sheet, GetBalanceSheetResult result, int startRow) {
        Row titleRow = sheet.createRow(startRow);
        titleRow.createCell(0).setCellValue("貸借対照表");
        if (result.date() != null) {
            titleRow.createCell(1).setCellValue("基準日: " + result.date());
        }
        return startRow + 1;
    }

    private int writeExcelSection(Sheet sheet, BalanceSheetSection section, int startRow,
                                   CellStyle headerStyle, CellStyle currencyStyle) {
        int currentRow = startRow;
        Row sectionRow = sheet.createRow(currentRow++);
        sectionRow.createCell(0).setCellValue(section.sectionDisplayName());
        sectionRow.getCell(0).setCellStyle(headerStyle);

        for (BalanceSheetEntry entry : section.entries()) {
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

    private void writeExcelTotals(Sheet sheet, GetBalanceSheetResult result, int startRow,
                                   CellStyle currencyStyle) {
        Row totalRow = sheet.createRow(startRow);
        totalRow.createCell(0).setCellValue("資産合計");
        totalRow.createCell(1).setCellValue(result.totalAssets().doubleValue());
        totalRow.getCell(1).setCellStyle(currencyStyle);

        Row liabEquityRow = sheet.createRow(startRow + 1);
        liabEquityRow.createCell(0).setCellValue("負債・純資産合計");
        liabEquityRow.createCell(1).setCellValue(result.totalLiabilitiesAndEquity().doubleValue());
        liabEquityRow.getCell(1).setCellStyle(currencyStyle);
    }

    private void writePdfTitle(Document document, GetBalanceSheetResult result,
                                Font titleFont, Font bodyFont) throws IOException {
        Paragraph title = new Paragraph("貸借対照表", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        if (result.date() != null) {
            Paragraph datePara = new Paragraph("基準日: " + result.date(), bodyFont);
            datePara.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(datePara);
        }
        document.add(new Paragraph(" "));
    }

    private void addPdfHeaderRow(PdfPTable table, Font font) {
        addPdfCell(table, "資産の部", font, true);
        addPdfCell(table, "金額", font, true);
        addPdfCell(table, "負債・純資産の部", font, true);
        addPdfCell(table, "金額", font, true);
    }

    private void writePdfSections(PdfPTable table, GetBalanceSheetResult result, Font font) {
        BalanceSheetSection assetSection = findSection(result, "ASSET");
        BalanceSheetSection liabilitySection = findSection(result, "LIABILITY");
        BalanceSheetSection equitySection = findSection(result, "EQUITY");

        int assetRows = assetSection != null ? assetSection.entries().size() + 2 : 0;
        int rightRows = countRightRows(liabilitySection, equitySection);
        int maxRows = Math.max(assetRows, rightRows);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.JAPAN);
        List<String[]> leftRowList = buildSectionRows(assetSection, nf);
        List<String[]> rightRowList = buildRightSectionRows(liabilitySection, equitySection, nf);

        for (int i = 0; i < maxRows; i++) {
            String[] left = i < leftRowList.size() ? leftRowList.get(i) : EMPTY_ROW;
            String[] right = i < rightRowList.size() ? rightRowList.get(i) : EMPTY_ROW;
            addPdfCell(table, left[0], font, false);
            addPdfCell(table, left[1], font, false);
            addPdfCell(table, right[0], font, false);
            addPdfCell(table, right[1], font, false);
        }
    }

    private List<String[]> buildSectionRows(BalanceSheetSection section, NumberFormat nf) {
        List<String[]> rows = new ArrayList<>();
        if (section == null) {
            return rows;
        }
        rows.add(new String[]{section.sectionDisplayName(), ""});
        for (BalanceSheetEntry entry : section.entries()) {
            String label = "  " + entry.accountCode() + " " + entry.accountName();
            rows.add(new String[]{label, nf.format(entry.amount())});
        }
        rows.add(new String[]{section.sectionDisplayName() + "合計", nf.format(section.subtotal())});
        return rows;
    }

    private List<String[]> buildRightSectionRows(BalanceSheetSection liabilitySection,
                                                  BalanceSheetSection equitySection,
                                                  NumberFormat nf) {
        List<String[]> rows = new ArrayList<>();
        if (liabilitySection != null) {
            rows.addAll(buildSectionRows(liabilitySection, nf));
        }
        if (equitySection != null) {
            rows.addAll(buildSectionRows(equitySection, nf));
        }
        return rows;
    }

    private void addPdfTotalRow(PdfPTable table, GetBalanceSheetResult result, Font font) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.JAPAN);
        addPdfCell(table, "資産合計", font, true);
        addPdfCell(table, nf.format(result.totalAssets()), font, true);
        addPdfCell(table, "負債・純資産合計", font, true);
        addPdfCell(table, nf.format(result.totalLiabilitiesAndEquity()), font, true);
    }

    private void addPdfCell(PdfPTable table, String text, Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        if (isHeader) {
            cell.setBackgroundColor(HEADER_BG);
        }
        cell.setPadding(4);
        table.addCell(cell);
    }

    private BalanceSheetSection findSection(GetBalanceSheetResult result, String sectionType) {
        return result.sections().stream()
                .filter(s -> s.sectionType().equals(sectionType))
                .findFirst()
                .orElse(null);
    }

    private int countRightRows(BalanceSheetSection liabilitySection, BalanceSheetSection equitySection) {
        int count = 0;
        if (liabilitySection != null) {
            count += liabilitySection.entries().size() + 2;
        }
        if (equitySection != null) {
            count += equitySection.entries().size() + 2;
        }
        return count;
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
