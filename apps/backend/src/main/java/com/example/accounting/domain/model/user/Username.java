package com.example.accounting.domain.model.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * ユーザー名を表す値オブジェクト
 *
 * <p>3〜50文字の制約を持つ。</p>
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Username {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 50;

    String value;

    /**
     * ユーザー名を生成する
     *
     * @param value ユーザー名
     * @return Username インスタンス
     * @throws IllegalArgumentException ユーザー名が不正な場合
     */
    public static Username of(String value) {
        validate(value);
        return new Username(value);
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

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ユーザー名は必須です");
        }
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("ユーザー名は%d〜%d文字で入力してください", MIN_LENGTH, MAX_LENGTH));
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
