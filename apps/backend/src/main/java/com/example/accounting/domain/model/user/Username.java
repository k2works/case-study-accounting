package com.example.accounting.domain.model.user;

import io.vavr.control.Either;

/**
 * ユーザー名を表す値オブジェクト
 *
 * <p>3〜50文字の制約を持つ。</p>
 */
public record Username(String value) {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 50;

    /**
     * ユーザー名を生成する
     *
     * @param value ユーザー名
     * @return Username インスタンス
     */
    public static Username of(String value) {
        return new Username(value);
    }

    /**
     * バリデーション付きファクトリメソッド
     *
     * @param value ユーザー名
     * @return Either（左: エラーメッセージ、右: Username インスタンス）
     */
    public static Either<String, Username> validated(String value) {
        if (value == null || value.isBlank()) {
            return Either.left("ユーザー名は必須です");
        }
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            return Either.left(
                    String.format("ユーザー名は%d〜%d文字で入力してください", MIN_LENGTH, MAX_LENGTH));
        }
        return Either.right(new Username(value));
    }

    /**
     * DB からの復元用ファクトリメソッド（バリデーションをスキップ）
     *
     * @param value ユーザー名
     * @return Username インスタンス
     */
    public static Username reconstruct(String value) {
        return new Username(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
