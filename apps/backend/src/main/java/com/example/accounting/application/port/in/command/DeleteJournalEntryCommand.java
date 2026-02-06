package com.example.accounting.application.port.in.command;

/**
 * 仕訳削除コマンド
 *
 * @param journalEntryId 仕訳ID
 */
public record DeleteJournalEntryCommand(
        Integer journalEntryId
) {
}
