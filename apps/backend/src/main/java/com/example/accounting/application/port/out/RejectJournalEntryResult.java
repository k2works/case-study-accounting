package com.example.accounting.application.port.out;

import java.time.LocalDateTime;

/**
 * 仕訳差し戻し結果
 */
public record RejectJournalEntryResult(
        boolean success,
        Integer journalEntryId,
        String status,
        String rejectedBy,
        LocalDateTime rejectedAt,
        String rejectionReason,
        String message,
        String errorMessage
) {
    public static RejectJournalEntryResult success(Integer journalEntryId, String status,
                                                    String rejectedBy, LocalDateTime rejectedAt,
                                                    String rejectionReason) {
        return new RejectJournalEntryResult(true, journalEntryId, status, rejectedBy, rejectedAt,
                rejectionReason, "仕訳を差し戻しました", null);
    }

    public static RejectJournalEntryResult failure(String errorMessage) {
        return new RejectJournalEntryResult(false, null, null, null, null, null, null, errorMessage);
    }
}
