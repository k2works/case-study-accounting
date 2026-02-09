package com.example.accounting.infrastructure.web.dto;

import java.time.LocalDateTime;

/**
 * 仕訳承認レスポンス
 */
public record ApproveJournalEntryResponse(
        boolean success,
        Integer journalEntryId,
        String status,
        String approvedBy,
        LocalDateTime approvedAt,
        String message,
        String errorMessage
) {
    public static ApproveJournalEntryResponse success(Integer journalEntryId, String status,
                                                      String approvedBy, LocalDateTime approvedAt, String message) {
        return new ApproveJournalEntryResponse(true, journalEntryId, status, approvedBy, approvedAt, message, null);
    }

    public static ApproveJournalEntryResponse failure(String errorMessage) {
        return new ApproveJournalEntryResponse(false, null, null, null, null, null, errorMessage);
    }
}
