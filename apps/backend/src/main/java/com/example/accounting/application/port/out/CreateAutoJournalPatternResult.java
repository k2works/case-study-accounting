package com.example.accounting.application.port.out;

public record CreateAutoJournalPatternResult(
        boolean success,
        Long patternId,
        String patternCode,
        String patternName,
        String errorMessage
) {
    public static CreateAutoJournalPatternResult success(Long patternId, String patternCode, String patternName) {
        return new CreateAutoJournalPatternResult(true, patternId, patternCode, patternName, null);
    }

    public static CreateAutoJournalPatternResult failure(String errorMessage) {
        return new CreateAutoJournalPatternResult(false, null, null, null, errorMessage);
    }
}
