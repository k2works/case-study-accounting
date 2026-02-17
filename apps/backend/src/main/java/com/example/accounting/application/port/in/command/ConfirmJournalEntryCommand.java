package com.example.accounting.application.port.in.command;

import io.vavr.control.Either;

public record ConfirmJournalEntryCommand(Integer journalEntryId, String confirmerId) {

    public static Either<String, ConfirmJournalEntryCommand> of(Integer journalEntryId, String confirmerId) {
        if (journalEntryId == null) {
            return Either.left("仕訳IDは必須です");
        }
        if (confirmerId == null || confirmerId.isBlank()) {
            return Either.left("確定者IDは必須です");
        }
        return Either.right(new ConfirmJournalEntryCommand(journalEntryId, confirmerId));
    }
}
