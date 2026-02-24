package com.example.accounting.domain.model.audit;

public enum AuditAction {
    LOGIN("ログイン"),
    LOGOUT("ログアウト"),
    CREATE("作成"),
    UPDATE("更新"),
    DELETE("削除"),
    APPROVE("承認"),
    REJECT("差戻し"),
    CONFIRM("確定");

    private final String displayName;

    AuditAction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
