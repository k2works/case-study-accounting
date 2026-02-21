package com.example.accounting.infrastructure.web.dto;

import java.time.LocalDate;

public record GenerateAutoJournalResponse(
        boolean success,
        Integer journalEntryId,
        LocalDate journalDate,
        String description,
        String status,
        String errorMessage
) {
    public static GenerateAutoJournalResponse success(
            Integer journalEntryId,
            LocalDate journalDate,
            String description,
            String status
    ) {
        return new GenerateAutoJournalResponse(true, journalEntryId, journalDate, description, status, null);
    }

    public static GenerateAutoJournalResponse failure(String errorMessage) {
        return new GenerateAutoJournalResponse(false, null, null, null, null, errorMessage);
    }
}
