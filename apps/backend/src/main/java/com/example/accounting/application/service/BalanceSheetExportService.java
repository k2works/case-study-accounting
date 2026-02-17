package com.example.accounting.application.service;

import com.example.accounting.application.port.out.GetBalanceSheetResult;
import com.example.accounting.application.port.out.GetBalanceSheetResult.BalanceSheetSection;
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
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@SuppressWarnings("PMD.AvoidMutableCollectionInstantiation") // OpenPDF の Paragraph/Phrase は new 必須
public class BalanceSheetExportService extends AbstractStatementExportService {

    private static final String[] EMPTY_ROW = {"", ""};

    public Try<byte[]> exportToExcel(GetBalanceSheetResult result) {
        return Try.of(() -> {
            try (Workbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("貸借対照表");
                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle currencyStyle = createCurrencyStyle(workbook);

                int currentRow = writeExcelTitle(sheet, result, 0);
                currentRow++;

                for (BalanceSheetSection section : result.sections()) {
                    currentRow = writeExcelSection(sheet, toSectionData(section),
                            currentRow, headerStyle, currencyStyle);
                    currentRow++;
                }

                currentRow++;
                writeExcelTotals(sheet, result, currentRow, currencyStyle);

                sheet.autoSizeColumn(0);
                sheet.autoSizeColumn(1);

                workbook.write(out);
                return out.toByteArray();
            }
        });
    }

    public Try<byte[]> exportToPdf(GetBalanceSheetResult result) {
        return Try.of(() -> {
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
        });
    }

    private static SectionExcelData toSectionData(BalanceSheetSection section) {
        return new SectionExcelData(
                section.sectionDisplayName(),
                section.entries().stream()
                        .map(e -> new EntryExcelData(e.accountCode(), e.accountName(), e.amount()))
                        .toList(),
                section.subtotal()
        );
    }

    private int writeExcelTitle(Sheet sheet, GetBalanceSheetResult result, int startRow) {
        Row titleRow = sheet.createRow(startRow);
        titleRow.createCell(0).setCellValue("貸借対照表");
        if (result.date() != null) {
            titleRow.createCell(1).setCellValue("基準日: " + result.date());
        }
        return startRow + 1;
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
                                Font titleFont, Font bodyFont) {
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

        IntStream.range(0, maxRows).forEach(i -> {
            String[] left = i < leftRowList.size() ? leftRowList.get(i) : EMPTY_ROW;
            String[] right = i < rightRowList.size() ? rightRowList.get(i) : EMPTY_ROW;
            addPdfCell(table, left[0], font, false);
            addPdfCell(table, left[1], font, false);
            addPdfCell(table, right[0], font, false);
            addPdfCell(table, right[1], font, false);
        });
    }

    private List<String[]> buildSectionRows(BalanceSheetSection section, NumberFormat nf) {
        if (section == null) {
            return List.of();
        }
        String[] headerRow = {section.sectionDisplayName(), ""};
        String[] subtotalRow = {section.sectionDisplayName() + "合計", nf.format(section.subtotal())};
        Stream<String[]> entries = section.entries().stream()
                .map(entry -> new String[]{
                        "  " + entry.accountCode() + " " + entry.accountName(),
                        nf.format(entry.amount())
                });

        return Stream.of(
                Stream.<String[]>of(headerRow),
                entries,
                Stream.<String[]>of(subtotalRow)
        ).flatMap(s -> s).toList();
    }

    private List<String[]> buildRightSectionRows(BalanceSheetSection liabilitySection,
                                                  BalanceSheetSection equitySection,
                                                  NumberFormat nf) {
        return Stream.of(
                buildSectionRows(liabilitySection, nf).stream(),
                buildSectionRows(equitySection, nf).stream()
        ).flatMap(s -> s).toList();
    }

    private void addPdfTotalRow(PdfPTable table, GetBalanceSheetResult result, Font font) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.JAPAN);
        addPdfCell(table, "資産合計", font, true);
        addPdfCell(table, nf.format(result.totalAssets()), font, true);
        addPdfCell(table, "負債・純資産合計", font, true);
        addPdfCell(table, nf.format(result.totalLiabilitiesAndEquity()), font, true);
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
}
