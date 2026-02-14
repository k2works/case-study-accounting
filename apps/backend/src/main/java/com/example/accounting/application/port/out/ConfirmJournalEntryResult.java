package com.example.accounting.application.port.out;

import java.time.LocalDateTime;

public record ConfirmJournalEntryResult(
        boolean success,
        Integer journalEntryId,
        String status,
        String confirmedBy,
        LocalDateTime confirmedAt,
        String message,
        String errorMessage
) {
    public static ConfirmJournalEntryResult success(Integer journalEntryId, String status,
                                                    String confirmedBy, LocalDateTime confirmedAt) {
        return new ConfirmJournalEntryResult(true, journalEntryId, status, confirmedBy, confirmedAt,
                "仕訳を確定しました", null);
    }

    public static ConfirmJournalEntryResult failure(String errorMessage) {
        return new ConfirmJournalEntryResult(false, null, null, null, null, null, errorMessage);
    }
}
