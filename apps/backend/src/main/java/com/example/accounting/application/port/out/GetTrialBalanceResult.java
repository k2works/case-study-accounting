package com.example.accounting.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 残高試算表照会結果
 */
public record GetTrialBalanceResult(
        LocalDate date,
        BigDecimal totalDebit,
        BigDecimal totalCredit,
        boolean balanced,
        BigDecimal difference,
        List<TrialBalanceEntry> entries,
        List<CategorySubtotal> categorySubtotals
) {
    public GetTrialBalanceResult {
        entries = entries == null ? List.of() : List.copyOf(entries);
        categorySubtotals = categorySubtotals == null ? List.of() : List.copyOf(categorySubtotals);
    }

    /**
     * 試算表の各勘定科目行
     */
    public record TrialBalanceEntry(
            String accountCode,
            String accountName,
            String bsplCategory,
            String accountType,
            BigDecimal debitBalance,
            BigDecimal creditBalance
    ) {
    }

    /**
     * 勘定科目種別ごとの小計
     */
    public record CategorySubtotal(
            String accountType,
            String accountTypeDisplayName,
            BigDecimal debitSubtotal,
            BigDecimal creditSubtotal
    ) {
    }
}
