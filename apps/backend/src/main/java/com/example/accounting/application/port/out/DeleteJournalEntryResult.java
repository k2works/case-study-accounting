package com.example.accounting.application.port.out;

/**
 * 仕訳削除結果
 */
public record DeleteJournalEntryResult(
        boolean success,
        String errorMessage
) {
    public static DeleteJournalEntryResult ofSuccess() {
        return new DeleteJournalEntryResult(true, null);
    }

    public static DeleteJournalEntryResult ofFailure(String errorMessage) {
        return new DeleteJournalEntryResult(false, errorMessage);
    }
}
