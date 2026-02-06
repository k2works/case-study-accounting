package com.example.accounting.application.port.out;

import java.time.LocalDate;

/**
 * 仕訳編集結果
 */
public record UpdateJournalEntryResult(
        boolean success,
        Integer journalEntryId,
        LocalDate journalDate,
        String description,
        String status,
        Integer version,
        String errorMessage
) {
    public static UpdateJournalEntryResult success(Integer journalEntryId,
                                                   LocalDate journalDate,
                                                   String description,
                                                   String status,
                                                   Integer version) {
        return new UpdateJournalEntryResult(true, journalEntryId, journalDate,
                description, status, version, null);
    }

    public static UpdateJournalEntryResult failure(String errorMessage) {
        return new UpdateJournalEntryResult(false, null, null, null, null, null, errorMessage);
    }
}
