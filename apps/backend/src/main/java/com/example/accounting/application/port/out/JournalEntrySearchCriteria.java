package com.example.accounting.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 仕訳検索条件パラメータオブジェクト
 *
 * <p>リポジトリ検索メソッドに渡す検索条件をまとめたレコード。</p>
 */
public record JournalEntrySearchCriteria(
        List<String> statuses,
        LocalDate dateFrom,
        LocalDate dateTo,
        Integer accountId,
        BigDecimal amountFrom,
        BigDecimal amountTo,
        String description,
        int offset,
        int limit
) {
    public JournalEntrySearchCriteria {
        statuses = statuses == null ? List.of() : List.copyOf(statuses);
    }
}
