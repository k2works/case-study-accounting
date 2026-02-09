package com.example.accounting.application.port.in.command;

/**
 * 仕訳承認申請コマンド
 *
 * @param journalEntryId 仕訳ID
 */
public record SubmitForApprovalCommand(Integer journalEntryId) {
    public SubmitForApprovalCommand {
        if (journalEntryId == null) {
            throw new IllegalArgumentException("仕訳IDは必須です");
        }
    }
}
