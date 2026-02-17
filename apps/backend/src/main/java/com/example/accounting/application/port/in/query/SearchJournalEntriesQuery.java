package com.example.accounting.application.port.in.query;

import io.vavr.control.Either;

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
        statuses = statuses == null ? List.of() : List.copyOf(statuses);
    }

    public static Either<String, SearchJournalEntriesQuery> of(
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
        if (page < 0) {
            return Either.left("ページ番号は 0 以上である必要があります");
        }
        if (size < 1 || size > 100) {
            return Either.left("ページサイズは 1 以上 100 以下である必要があります");
        }
        return Either.right(new SearchJournalEntriesQuery(page, size, statuses, dateFrom, dateTo, accountId, amountFrom, amountTo, description));
    }

    public static SearchJournalEntriesQuery defaultQuery() {
        return new SearchJournalEntriesQuery(0, 20, List.of(), null, null, null, null, null, null);
    }
}
