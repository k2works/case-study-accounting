package com.example.accounting.infrastructure.web.dto;

import java.time.LocalDateTime;

public record ConfirmJournalEntryResponse(
        boolean success,
        Integer journalEntryId,
        String status,
        String confirmedBy,
        LocalDateTime confirmedAt,
        String message,
        String errorMessage
) {
    public static ConfirmJournalEntryResponse success(Integer journalEntryId, String status,
                                                      String confirmedBy, LocalDateTime confirmedAt, String message) {
        return new ConfirmJournalEntryResponse(true, journalEntryId, status, confirmedBy, confirmedAt, message, null);
    }

    public static ConfirmJournalEntryResponse failure(String errorMessage) {
        return new ConfirmJournalEntryResponse(false, null, null, null, null, null, errorMessage);
    }
}
