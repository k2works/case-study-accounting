package com.example.accounting.domain.model.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.regex.Pattern;

/**
 * メールアドレスを表す値オブジェクト
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Email {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    String value;

    /**
     * メールアドレスを生成する
     *
     * @param value メールアドレス
     * @return Email インスタンス
     * @throws IllegalArgumentException メールアドレスが不正な場合
     */
    public static Email of(String value) {
        validate(value);
        return new Email(value);
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

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("メールアドレスは必須です");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("メールアドレスの形式が不正です");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
