package com.example.accounting.application.port.out;

import java.time.LocalDate;

public record GenerateAutoJournalResult(
        boolean success,
        Integer journalEntryId,
        LocalDate journalDate,
        String description,
        String status,
        String errorMessage
) {
    public static GenerateAutoJournalResult success(
            Integer journalEntryId,
            LocalDate journalDate,
            String description,
            String status
    ) {
        return new GenerateAutoJournalResult(true, journalEntryId, journalDate, description, status, null);
    }

    public static GenerateAutoJournalResult failure(String errorMessage) {
        return new GenerateAutoJournalResult(false, null, null, null, null, errorMessage);
    }
}
