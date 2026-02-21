package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.auto_journal.AutoJournalLog;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AutoJournalLogEntity {
    private Long id;
    private Long patternId;
    private OffsetDateTime executedAt;
    private Integer processedCount;
    private Integer generatedCount;
    private String status;
    private String message;
    private String errorDetail;

    public static AutoJournalLogEntity fromDomain(AutoJournalLog log) {
        AutoJournalLogEntity entity = new AutoJournalLogEntity();
        entity.setId(log.getId());
        entity.setPatternId(log.getPatternId());
        entity.setExecutedAt(log.getExecutedAt());
        entity.setProcessedCount(log.getProcessedCount());
        entity.setGeneratedCount(log.getGeneratedCount());
        entity.setStatus(log.getStatus());
        entity.setMessage(log.getMessage());
        entity.setErrorDetail(log.getErrorDetail());
        return entity;
    }

    public AutoJournalLog toDomain() {
        return AutoJournalLog.reconstruct(id, patternId, executedAt, processedCount, generatedCount, status, message,
                errorDetail);
    }
}
