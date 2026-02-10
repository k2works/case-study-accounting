package com.example.accounting.application.port.out;

import java.math.BigDecimal;
import java.util.List;

public record GetMonthlyBalanceResult(
        String accountCode,
        String accountName,
        Integer fiscalPeriod,
        BigDecimal openingBalance,
        BigDecimal debitTotal,
        BigDecimal creditTotal,
        BigDecimal closingBalance,
        List<MonthlyBalanceEntry> entries
) {
    public GetMonthlyBalanceResult {
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public record MonthlyBalanceEntry(
            int month,
            BigDecimal openingBalance,
            BigDecimal debitAmount,
            BigDecimal creditAmount,
            BigDecimal closingBalance
    ) {
    }
}
