package com.example.accounting.domain.model.journal;

import java.util.Arrays;

/**
 * 仕訳ステータスを表す列挙型
 */
public enum JournalEntryStatus {
    DRAFT("下書き"),
    PENDING("承認待ち"),
    APPROVED("承認済み"),
    CONFIRMED("確定済み");

    private final String displayName;

    JournalEntryStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * コードから仕訳ステータスを取得する
     *
     * @param code ステータスコード
     * @return 対応する仕訳ステータス
     * @throws IllegalArgumentException 無効なコードの場合
     */
    public static JournalEntryStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.name().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "無効な仕訳ステータスコードです: " + code));
    }

    public String getDisplayName() {
        return displayName;
    }
}
