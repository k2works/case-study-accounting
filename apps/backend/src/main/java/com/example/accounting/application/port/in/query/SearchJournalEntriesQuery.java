package com.example.accounting.application.port.in.query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 仕訳検索クエリ
 */
public record SearchJournalEntriesQuery(
        int page,
        int size,
        List<String> statuses,
        LocalDate dateFrom,
        LocalDate dateTo,
        Integer accountId,
        BigDecimal amountFrom,
        BigDecimal amountTo,
        String description
) {
    public SearchJournalEntriesQuery {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        statuses = statuses == null ? List.of() : List.copyOf(statuses);
    }

    public static SearchJournalEntriesQuery defaultQuery() {
        return new SearchJournalEntriesQuery(0, 20, List.of(), null, null, null, null, null, null);
    }
}
