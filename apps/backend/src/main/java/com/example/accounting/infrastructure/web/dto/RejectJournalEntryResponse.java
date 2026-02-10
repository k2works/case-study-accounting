package com.example.accounting.infrastructure.web.dto;

import java.time.LocalDateTime;

/**
 * 仕訳差し戻しレスポンス
 */
public record RejectJournalEntryResponse(
        boolean success,
        Integer journalEntryId,
        String status,
        String rejectedBy,
        LocalDateTime rejectedAt,
        String rejectionReason,
        String message,
        String errorMessage
) {
    public static RejectJournalEntryResponse success(Integer journalEntryId, String status,
                                                     String rejectedBy, LocalDateTime rejectedAt,
                                                     String rejectionReason, String message) {
        return new RejectJournalEntryResponse(true, journalEntryId, status, rejectedBy, rejectedAt,
                rejectionReason, message, null);
    }

    public static RejectJournalEntryResponse failure(String errorMessage) {
        return new RejectJournalEntryResponse(false, null, null, null, null, null, null, errorMessage);
    }
}
