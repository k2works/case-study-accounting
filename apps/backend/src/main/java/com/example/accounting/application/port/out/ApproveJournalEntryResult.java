package com.example.accounting.application.port.out;

import java.time.LocalDateTime;

/**
 * 仕訳承認結果
 */
public record ApproveJournalEntryResult(
        boolean success,
        Integer journalEntryId,
        String status,
        String approvedBy,
        LocalDateTime approvedAt,
        String message,
        String errorMessage
) {
    public static ApproveJournalEntryResult success(Integer journalEntryId, String status,
                                                    String approvedBy, LocalDateTime approvedAt) {
        return new ApproveJournalEntryResult(true, journalEntryId, status, approvedBy, approvedAt,
                "仕訳を承認しました", null);
    }

    public static ApproveJournalEntryResult failure(String errorMessage) {
        return new ApproveJournalEntryResult(false, null, null, null, null, null, errorMessage);
    }
}
