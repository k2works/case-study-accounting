package com.example.accounting.application.port.in.command;

/**
 * 仕訳差し戻しコマンド
 *
 * @param journalEntryId 仕訳ID
 * @param rejectorId 差し戻し者ID
 * @param rejectionReason 差し戻し理由
 */
public record RejectJournalEntryCommand(Integer journalEntryId, String rejectorId, String rejectionReason) {
    public RejectJournalEntryCommand {
        if (journalEntryId == null) {
            throw new IllegalArgumentException("仕訳IDは必須です");
        }
        if (rejectorId == null || rejectorId.isBlank()) {
            throw new IllegalArgumentException("差し戻し者IDは必須です");
        }
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new IllegalArgumentException("差し戻し理由は必須です");
        }
    }
}
