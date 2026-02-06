package com.example.accounting.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record GetJournalEntriesResult(
    List<JournalEntrySummary> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public GetJournalEntriesResult {
        content = content == null ? List.of() : List.copyOf(content);
    }

    public static GetJournalEntriesResult empty(int page, int size) {
        return new GetJournalEntriesResult(List.of(), page, size, 0L, 0);
    }

    public record JournalEntrySummary(
        Integer journalEntryId,
        LocalDate journalDate,
        String description,
        BigDecimal totalDebitAmount,
        BigDecimal totalCreditAmount,
        String status,
        Integer version
    ) {
    }
}
