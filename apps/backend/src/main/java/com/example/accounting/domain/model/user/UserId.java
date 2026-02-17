package com.example.accounting.domain.model.user;

import io.vavr.control.Either;

import java.util.UUID;

/**
 * ユーザーIDを表す値オブジェクト
 *
 * <p>UUID を使用して一意性を保証する。</p>
 */
public record UserId(String value) {

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

    /**
     * バリデーション付きファクトリメソッド
     *
     * @param value ユーザーID文字列
     * @return Either（左: エラーメッセージ、右: UserId インスタンス）
     */
    public static Either<String, UserId> validated(String value) {
        if (value == null) {
            return Either.left("ユーザーIDは必須です");
        }
        if (value.isBlank()) {
            return Either.left("ユーザーIDは空にできません");
        }
        return Either.right(new UserId(value));
    }
}
