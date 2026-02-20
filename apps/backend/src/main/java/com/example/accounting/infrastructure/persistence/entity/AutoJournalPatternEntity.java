package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.auto_journal.AutoJournalPattern;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternId;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternItem;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 自動仕訳パターンエンティティ（永続化用）
 */
@SuppressWarnings({"PMD.AvoidMutableCollectionInstantiation", "PMD.BooleanGetMethodName"})
public class AutoJournalPatternEntity {

    private Long id;
    private String patternCode;
    private String patternName;
    private String sourceTableName;
    private String description;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    // MyBatis が collection マッピングで要素を追加できるように可変リストで初期化
    private List<AutoJournalPatternItemEntity> items = new ArrayList<>();

    /**
     * ドメインモデルからエンティティを生成する
     */
    public static AutoJournalPatternEntity fromDomain(AutoJournalPattern pattern) {
        AutoJournalPatternEntity entity = new AutoJournalPatternEntity();
        if (pattern.getId() != null) {
            entity.setId(pattern.getId().value());
        }
        entity.setPatternCode(pattern.getPatternCode());
        entity.setPatternName(pattern.getPatternName());
        entity.setSourceTableName(pattern.getSourceTableName());
        entity.setDescription(pattern.getDescription());
        entity.setIsActive(pattern.getIsActive());
        entity.setItems(pattern.getItems().stream()
                .map(item -> AutoJournalPatternItemEntity.fromDomain(item, entity.getId()))
                .toList());
        return entity;
    }

    /**
     * エンティティからドメインモデルを再構築する
     */
    public AutoJournalPattern toDomain() {
        List<AutoJournalPatternItem> domainItems = items == null
                ? List.of()
                : items.stream().map(AutoJournalPatternItemEntity::toDomain).toList();

        return AutoJournalPattern.reconstruct(
                id == null ? null : AutoJournalPatternId.of(id),
                patternCode,
                patternName,
                sourceTableName,
                description,
                isActive,
                domainItems
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatternCode() {
        return patternCode;
    }

    public void setPatternCode(String patternCode) {
        this.patternCode = patternCode;
    }

    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

    public void setSourceTableName(String sourceTableName) {
        this.sourceTableName = sourceTableName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
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

    @SuppressFBWarnings(value = "EI_EXPOSE_REP",
            justification = "MyBatisのcollectionマッピングが直接このリストに追加するため、可変リストを返す必要がある")
    public List<AutoJournalPatternItemEntity> getItems() {
        return items;
    }

    public void setItems(List<AutoJournalPatternItemEntity> items) {
        // MyBatis が collection マッピングで要素を追加できるように ArrayList を使用
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }
}
