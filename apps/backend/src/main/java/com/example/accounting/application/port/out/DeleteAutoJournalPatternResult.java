package com.example.accounting.application.port.out;

public record DeleteAutoJournalPatternResult(
        boolean success,
        Long patternId,
        String message,
        String errorMessage
) {
    public static DeleteAutoJournalPatternResult success(Long patternId) {
        return new DeleteAutoJournalPatternResult(true, patternId, "自動仕訳パターンを削除しました", null);
    }

    public static DeleteAutoJournalPatternResult failure(String errorMessage) {
        return new DeleteAutoJournalPatternResult(false, null, null, errorMessage);
    }
}
