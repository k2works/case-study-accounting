package com.example.accounting.domain.model.auto_journal;

import java.util.Objects;

public record AutoJournalPatternId(Long value) {
    public AutoJournalPatternId {
        Objects.requireNonNull(value, "自動仕訳パターンIDは必須です");
    }

    public static AutoJournalPatternId of(Long value) {
        return new AutoJournalPatternId(value);
    }
}
