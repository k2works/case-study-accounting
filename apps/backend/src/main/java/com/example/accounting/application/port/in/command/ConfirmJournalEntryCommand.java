package com.example.accounting.application.port.in.command;

public record ConfirmJournalEntryCommand(Integer journalEntryId, String confirmerId) {
    public ConfirmJournalEntryCommand {
        if (journalEntryId == null) {
            throw new IllegalArgumentException("仕訳IDは必須です");
        }
        if (confirmerId == null || confirmerId.isBlank()) {
            throw new IllegalArgumentException("確定者IDは必須です");
        }
    }
}
