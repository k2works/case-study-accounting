package com.example.accounting.domain.model.user;

import java.util.Objects;
import java.util.UUID;

/**
 * ユーザーIDを表す値オブジェクト
 *
 * <p>UUID を使用して一意性を保証する。</p>
 */
public record UserId(String value) {

    public UserId {
        Objects.requireNonNull(value, "ユーザーIDは必須です");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ユーザーIDは空にできません");
        }
    }

    /**
     * 新しいユーザーIDを生成する
     *
     * @return 新しいユーザーID
     */
    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    /**
     * 文字列からユーザーIDを生成する
     *
     * @param value ユーザーID文字列
     * @return ユーザーID
     */
    public static UserId of(String value) {
        return new UserId(value);
    }
}
