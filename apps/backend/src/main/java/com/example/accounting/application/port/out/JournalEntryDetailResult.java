package com.example.accounting.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 仕訳詳細結果
 */
public record JournalEntryDetailResult(
        Integer journalEntryId,
        LocalDate journalDate,
        String description,
        String status,
        Integer version,
        List<JournalEntryLineDetail> lines
) {
    /**
     * コンパクトコンストラクタ - 防御的コピーを作成
     */
    public JournalEntryDetailResult {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    public record JournalEntryLineDetail(
            Integer lineNumber,
            Integer accountId,
            String accountCode,
            String accountName,
            BigDecimal debitAmount,
            BigDecimal creditAmount
    ) {
    }
}
