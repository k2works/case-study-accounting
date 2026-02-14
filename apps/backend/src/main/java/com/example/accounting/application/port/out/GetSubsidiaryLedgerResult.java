package com.example.accounting.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record GetSubsidiaryLedgerResult(
        List<SubsidiaryLedgerEntry> content,
        String accountCode,
        String accountName,
        String subAccountCode,
        BigDecimal openingBalance,
        BigDecimal debitTotal,
        BigDecimal creditTotal,
        BigDecimal closingBalance,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public GetSubsidiaryLedgerResult {
        content = content == null ? List.of() : List.copyOf(content);
    }

    public record SubsidiaryLedgerEntry(
            Integer journalEntryId,
            LocalDate journalDate,
            String description,
            BigDecimal debitAmount,
            BigDecimal creditAmount,
            BigDecimal runningBalance
    ) {
    }
}
