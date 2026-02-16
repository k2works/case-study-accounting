package com.example.accounting.domain.model.user;

import io.vavr.control.Either;

import java.util.regex.Pattern;

/**
 * メールアドレスを表す値オブジェクト
 */
public record Email(String value) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * メールアドレスを生成する
     *
     * @param value メールアドレス
     * @return Email インスタンス
     */
    public static Email of(String value) {
        return new Email(value);
    }

    /**
     * バリデーション付きファクトリメソッド
     *
     * @param value メールアドレス
     * @return Either（左: エラーメッセージ、右: Email インスタンス）
     */
    public static Either<String, Email> validated(String value) {
        if (value == null || value.isBlank()) {
            return Either.left("メールアドレスは必須です");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            return Either.left("メールアドレスの形式が不正です");
        }
        return Either.right(new Email(value));
    }

    /**
     * DB からの復元用ファクトリメソッド（バリデーションをスキップ）
     *
     * @param value メールアドレス
     * @return Email インスタンス
     */
    public static Email reconstruct(String value) {
        return new Email(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
