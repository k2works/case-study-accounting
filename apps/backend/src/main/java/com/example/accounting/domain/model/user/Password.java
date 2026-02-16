package com.example.accounting.domain.model.user;

import io.vavr.control.Either;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * パスワードを表す値オブジェクト
 *
 * <p>ハッシュ化されたパスワードを保持する。
 * 生のパスワードは保持せず、検証のみ可能。</p>
 */
public record Password(String value) {

    private static final int MIN_LENGTH = 8;
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /**
     * 生のパスワードからハッシュ化されたパスワードを生成する
     *
     * @param rawPassword 平文パスワード
     * @return Password インスタンス
     */
    public static Password fromRawPassword(String rawPassword) {
        String hashed = PASSWORD_ENCODER.encode(rawPassword);
        return new Password(hashed);
    }

    /**
     * バリデーション付きファクトリメソッド（生のパスワードからハッシュ化）
     *
     * @param rawPassword 平文パスワード
     * @return Either（左: エラーメッセージ、右: Password インスタンス）
     */
    public static Either<String, Password> validated(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return Either.left("パスワードは必須です");
        }
        if (rawPassword.length() < MIN_LENGTH) {
            return Either.left(
                    String.format("パスワードは%d文字以上で入力してください", MIN_LENGTH));
        }
        String hashed = PASSWORD_ENCODER.encode(rawPassword);
        return Either.right(new Password(hashed));
    }

    /**
     * DB からの復元用ファクトリメソッド（バリデーションをスキップ）
     *
     * @param hashedValue ハッシュ化されたパスワード
     * @return Password インスタンス
     */
    public static Password reconstruct(String hashedValue) {
        return new Password(hashedValue);
    }

    /**
     * 平文パスワードがこのパスワードと一致するかを検証する
     *
     * @param rawPassword 平文パスワード
     * @return 一致する場合 true
     */
    public boolean matches(String rawPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, this.value);
    }

    @Override
    public String toString() {
        return "********";
    }
}
