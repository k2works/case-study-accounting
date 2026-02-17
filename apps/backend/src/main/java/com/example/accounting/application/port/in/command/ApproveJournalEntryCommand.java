package com.example.accounting.application.port.in.command;

import io.vavr.control.Either;

/**
 * 仕訳承認コマンド
 *
 * @param journalEntryId 仕訳ID
 * @param approverId 承認者ID
 */
public record ApproveJournalEntryCommand(Integer journalEntryId, String approverId) {

    public static Either<String, ApproveJournalEntryCommand> of(Integer journalEntryId, String approverId) {
        if (journalEntryId == null) {
            return Either.left("仕訳IDは必須です");
        }
        if (approverId == null || approverId.isBlank()) {
            return Either.left("承認者IDは必須です");
        }
        return Either.right(new ApproveJournalEntryCommand(journalEntryId, approverId));
    }
}
