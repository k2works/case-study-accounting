package com.example.accounting.domain.model.auto_journal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.OffsetDateTime;
import java.util.Objects;

@Value
@With
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AutoJournalLog {
    Long id;
    Long patternId;
    OffsetDateTime executedAt;
    Integer processedCount;
    Integer generatedCount;
    String status;
    String message;
    String errorDetail;

    public static AutoJournalLog createSuccess(Long patternId, String message) {
        Objects.requireNonNull(patternId, "パターンIDは必須です");
        return new AutoJournalLog(null, patternId, OffsetDateTime.now(), 1, 1, "SUCCESS", message, null);
    }

    public static AutoJournalLog createFailure(Long patternId, String message, String errorDetail) {
        Objects.requireNonNull(patternId, "パターンIDは必須です");
        return new AutoJournalLog(null, patternId, OffsetDateTime.now(), 1, 0, "FAILED", message, errorDetail);
    }

    public static AutoJournalLog reconstruct(Long id,
                                             Long patternId,
                                             OffsetDateTime executedAt,
                                             Integer processedCount,
                                             Integer generatedCount,
                                             String status,
                                             String message,
                                             String errorDetail) {
        return new AutoJournalLog(id, patternId, executedAt, processedCount, generatedCount, status, message,
                errorDetail);
    }
}
