package com.example.accounting.application.port.in.command;

import io.vavr.control.Either;

/**
 * 仕訳承認申請コマンド
 *
 * @param journalEntryId 仕訳ID
 */
public record SubmitForApprovalCommand(Integer journalEntryId) {

    public static Either<String, SubmitForApprovalCommand> of(Integer journalEntryId) {
        if (journalEntryId == null) {
            return Either.left("仕訳IDは必須です");
        }
        return Either.right(new SubmitForApprovalCommand(journalEntryId));
    }
}
