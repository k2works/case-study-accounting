package com.example.accounting.application.port.in.command;

/**
 * 仕訳承認コマンド
 *
 * @param journalEntryId 仕訳ID
 * @param approverId 承認者ID
 */
public record ApproveJournalEntryCommand(Integer journalEntryId, String approverId) {
    public ApproveJournalEntryCommand {
        if (journalEntryId == null) {
            throw new IllegalArgumentException("仕訳IDは必須です");
        }
        if (approverId == null || approverId.isBlank()) {
            throw new IllegalArgumentException("承認者IDは必須です");
        }
    }
}
