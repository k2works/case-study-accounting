package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;
import com.example.accounting.domain.model.journal.JournalEntryLine;
import com.example.accounting.domain.model.journal.JournalEntryStatus;
import com.example.accounting.domain.model.user.UserId;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 仕訳エンティティ（永続化用）
 */
public class JournalEntryEntity {

    private Integer id;
    private LocalDate journalDate;
    private String description;
    private String status;
    private String createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<JournalEntryLineEntity> lines;

    /**
     * ドメインモデルからエンティティを生成する
     */
    public static JournalEntryEntity fromDomain(JournalEntry journalEntry) {
        JournalEntryEntity entity = new JournalEntryEntity();
        if (journalEntry.getId() != null) {
            entity.setId(journalEntry.getId().value());
        }
        entity.setJournalDate(journalEntry.getJournalDate());
        entity.setDescription(journalEntry.getDescription());
        entity.setStatus(journalEntry.getStatus().name());
        if (journalEntry.getCreatedBy() != null) {
            entity.setCreatedBy(journalEntry.getCreatedBy().value());
        }
        entity.setCreatedAt(toOffsetDateTime(journalEntry.getCreatedAt()));
        entity.setUpdatedAt(toOffsetDateTime(journalEntry.getUpdatedAt()));
        entity.setLines(journalEntry.getLines().stream()
                .map(line -> JournalEntryLineEntity.fromDomain(line, entity.getId()))
                .toList());
        return entity;
    }

    /**
     * エンティティからドメインモデルを再構築する
     */
    public JournalEntry toDomain() {
        List<JournalEntryLine> domainLines = lines == null
                ? List.of()
                : lines.stream().map(JournalEntryLineEntity::toDomain).toList();
        return JournalEntry.reconstruct(
                JournalEntryId.of(id),
                journalDate,
                description,
                JournalEntryStatus.fromCode(status),
                domainLines,
                createdBy == null ? null : UserId.of(createdBy),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                updatedAt == null ? null : updatedAt.toLocalDateTime()
        );
    }

    private static OffsetDateTime toOffsetDateTime(java.time.LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getJournalDate() {
        return journalDate;
    }

    public void setJournalDate(LocalDate journalDate) {
        this.journalDate = journalDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<JournalEntryLineEntity> getLines() {
        return lines == null ? List.of() : List.copyOf(lines);
    }

    public void setLines(List<JournalEntryLineEntity> lines) {
        this.lines = lines == null ? List.of() : List.copyOf(lines);
    }
}
