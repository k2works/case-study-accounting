package com.example.accounting.application.port.out;

public record UpdateAutoJournalPatternResult(
        boolean success,
        Long patternId,
        String patternCode,
        String patternName,
        String message,
        String errorMessage
) {
    public static UpdateAutoJournalPatternResult success(Long patternId,
                                                         String patternCode,
                                                         String patternName,
                                                         String message) {
        return new UpdateAutoJournalPatternResult(true, patternId, patternCode, patternName, message, null);
    }

    public static UpdateAutoJournalPatternResult failure(String errorMessage) {
        return new UpdateAutoJournalPatternResult(false, null, null, null, null, errorMessage);
    }
}
