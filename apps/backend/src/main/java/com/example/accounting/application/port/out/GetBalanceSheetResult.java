package com.example.accounting.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 貸借対照表照会結果
 */
public record GetBalanceSheetResult(
        LocalDate date,
        LocalDate comparativeDate,
        List<BalanceSheetSection> sections,
        BigDecimal totalAssets,
        BigDecimal totalLiabilities,
        BigDecimal totalEquity,
        BigDecimal totalLiabilitiesAndEquity,
        boolean balanced,
        BigDecimal difference
) {
    public GetBalanceSheetResult {
        sections = sections == null ? List.of() : List.copyOf(sections);
    }

    /**
     * 貸借対照表のセクション（資産・負債・純資産）
     */
    public record BalanceSheetSection(
            String sectionType,
            String sectionDisplayName,
            List<BalanceSheetEntry> entries,
            BigDecimal subtotal,
            ComparativeData comparativeSubtotal
    ) {
        public BalanceSheetSection {
            entries = entries == null ? List.of() : List.copyOf(entries);
        }
    }

    /**
     * 貸借対照表の各勘定科目行
     */
    public record BalanceSheetEntry(
            String accountCode,
            String accountName,
            String accountType,
            BigDecimal amount,
            ComparativeData comparative
    ) {
    }

    /**
     * 前期比較データ
     */
    public record ComparativeData(
            BigDecimal previousAmount,
            BigDecimal difference,
            BigDecimal changeRate
    ) {
    }
}
