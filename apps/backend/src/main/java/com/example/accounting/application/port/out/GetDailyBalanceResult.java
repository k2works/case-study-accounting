package com.example.accounting.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record GetDailyBalanceResult(
        Integer accountId,
        String accountCode,
        String accountName,
        BigDecimal openingBalance,
        BigDecimal debitTotal,
        BigDecimal creditTotal,
        BigDecimal closingBalance,
        List<DailyBalanceEntry> entries
) {
    public GetDailyBalanceResult {
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public record DailyBalanceEntry(
            LocalDate date,
            BigDecimal debitTotal,
            BigDecimal creditTotal,
            BigDecimal balance,
            long transactionCount
    ) {
    }
}
