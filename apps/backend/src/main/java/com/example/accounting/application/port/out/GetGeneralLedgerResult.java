package com.example.accounting.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record GetGeneralLedgerResult(
        List<GeneralLedgerEntry> content,
        Integer accountId,
        String accountCode,
        String accountName,
        BigDecimal openingBalance,
        BigDecimal debitTotal,
        BigDecimal creditTotal,
        BigDecimal closingBalance,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public GetGeneralLedgerResult {
        content = content == null ? List.of() : List.copyOf(content);
    }

    public record GeneralLedgerEntry(
            Integer journalEntryId,
            LocalDate journalDate,
            String description,
            BigDecimal debitAmount,
            BigDecimal creditAmount,
            BigDecimal runningBalance
    ) {
    }
}
