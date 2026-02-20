package com.example.accounting.domain.model.auto_journal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Value
@With
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AutoJournalPattern {
    AutoJournalPatternId id;
    String patternCode;
    String patternName;
    String sourceTableName;
    String description;
    Boolean isActive;
    List<AutoJournalPatternItem> items;

    public static AutoJournalPattern create(String patternCode,
                                            String patternName,
                                            String sourceTableName,
                                            String description) {
        Objects.requireNonNull(patternCode, "パターンコードは必須です");
        Objects.requireNonNull(patternName, "パターン名は必須です");
        Objects.requireNonNull(sourceTableName, "ソーステーブル名は必須です");
        return new AutoJournalPattern(null, patternCode, patternName, sourceTableName, description, true, List.of());
    }

    public static AutoJournalPattern reconstruct(AutoJournalPatternId id,
                                                 String patternCode,
                                                 String patternName,
                                                 String sourceTableName,
                                                 String description,
                                                 Boolean isActive,
                                                 List<AutoJournalPatternItem> items) {
        return new AutoJournalPattern(
                id,
                patternCode,
                patternName,
                sourceTableName,
                description,
                isActive,
                items == null ? List.of() : List.copyOf(items)
        );
    }

    public AutoJournalPattern addItem(AutoJournalPatternItem item) {
        Objects.requireNonNull(item, "明細は必須です");
        return this.withItems(Stream.concat(items.stream(), Stream.of(item)).toList());
    }

    public AutoJournalPattern activate() {
        return this.withIsActive(true);
    }

    public AutoJournalPattern deactivate() {
        return this.withIsActive(false);
    }
}
