package com.example.accounting.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 損益計算書照会結果
 */
public record GetProfitAndLossResult(
        LocalDate dateFrom,
        LocalDate dateTo,
        LocalDate comparativeDateFrom,
        LocalDate comparativeDateTo,
        List<ProfitAndLossSection> sections,
        BigDecimal totalRevenue,
        BigDecimal totalExpense,
        BigDecimal netIncome
) {
    public GetProfitAndLossResult {
        sections = sections == null ? List.of() : List.copyOf(sections);
    }

    /**
     * 損益計算書のセクション（収益・費用）
     */
    public record ProfitAndLossSection(
            String sectionType,
            String sectionDisplayName,
            List<ProfitAndLossEntry> entries,
            BigDecimal subtotal,
            ComparativeData comparativeSubtotal
    ) {
        public ProfitAndLossSection {
            entries = entries == null ? List.of() : List.copyOf(entries);
        }
    }

    /**
     * 損益計算書の各勘定科目行
     */
    public record ProfitAndLossEntry(
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
