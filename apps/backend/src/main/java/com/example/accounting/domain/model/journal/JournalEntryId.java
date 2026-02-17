package com.example.accounting.domain.model.journal;

import io.vavr.control.Either;

import java.util.UUID;

/**
 * 仕訳 ID を表す値オブジェクト
 *
 * <p>UUID を利用して一意な Integer 値を生成する。</p>
 */
public record JournalEntryId(Integer value) {

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

    /**
     * バリデーション付きファクトリメソッド
     *
     * @param value 仕訳 ID
     * @return Either（左: エラーメッセージ、右: JournalEntryId インスタンス）
     */
    public static Either<String, JournalEntryId> validated(Integer value) {
        if (value == null) {
            return Either.left("仕訳 ID は必須です");
        }
        return Either.right(new JournalEntryId(value));
    }
}
