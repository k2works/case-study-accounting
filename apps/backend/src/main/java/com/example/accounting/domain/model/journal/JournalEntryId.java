package com.example.accounting.domain.model.journal;

import java.util.UUID;

/**
 * 仕訳 ID を表す値オブジェクト
 *
 * <p>UUID を利用して一意な Integer 値を生成する。</p>
 */
public record JournalEntryId(Integer value) {

    public JournalEntryId {
        if (value == null) {
            throw new IllegalArgumentException("仕訳 ID は必須です");
        }
    }

    /**
     * 新しい仕訳 ID を生成する
     *
     * @return 新しい仕訳 ID
     */
    public static JournalEntryId generate() {
        return new JournalEntryId(UUID.randomUUID().hashCode());
    }

    /**
     * Integer から仕訳 ID を生成する
     *
     * @param value 仕訳 ID
     * @return 仕訳 ID
     */
    public static JournalEntryId of(Integer value) {
        return new JournalEntryId(value);
    }
}
