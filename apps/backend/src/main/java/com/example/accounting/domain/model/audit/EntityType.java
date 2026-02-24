package com.example.accounting.domain.model.audit;

public enum EntityType {
    JOURNAL_ENTRY("仕訳"),
    ACCOUNT("勘定科目"),
    USER("ユーザー"),
    AUTO_JOURNAL_PATTERN("自動仕訳パターン");

    private final String displayName;

    EntityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
