package com.example.accounting.infrastructure.web.dto;

import com.example.accounting.application.port.out.DeleteAutoJournalPatternResult;

public record DeleteAutoJournalPatternResponse(
        boolean success,
        Long patternId,
        String message,
        String errorMessage
) {
    public static DeleteAutoJournalPatternResponse from(DeleteAutoJournalPatternResult result) {
        return new DeleteAutoJournalPatternResponse(
                result.success(),
                result.patternId(),
                result.message(),
                result.errorMessage()
        );
    }
}
