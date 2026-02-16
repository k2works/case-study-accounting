package com.example.accounting.application.port.in.command;

import io.vavr.control.Either;

/**
 * 仕訳差し戻しコマンド
 *
 * @param journalEntryId 仕訳ID
 * @param rejectorId 差し戻し者ID
 * @param rejectionReason 差し戻し理由
 */
public record RejectJournalEntryCommand(Integer journalEntryId, String rejectorId, String rejectionReason) {

    public static Either<String, RejectJournalEntryCommand> of(Integer journalEntryId, String rejectorId, String rejectionReason) {
        if (journalEntryId == null) {
            return Either.left("仕訳IDは必須です");
        }
        if (rejectorId == null || rejectorId.isBlank()) {
            return Either.left("差し戻し者IDは必須です");
        }
        if (rejectionReason == null || rejectionReason.isBlank()) {
            return Either.left("差し戻し理由は必須です");
        }
        return Either.right(new RejectJournalEntryCommand(journalEntryId, rejectorId, rejectionReason));
    }
}
