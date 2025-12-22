package com.example.accounting.domain.model.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * パスワードを表す値オブジェクト
 *
 * <p>ハッシュ化されたパスワードを保持する。
 * 生のパスワードは保持せず、検証のみ可能。</p>
 */
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Password {

    private static final int MIN_LENGTH = 8;
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final String hashedValue;

    /**
     * 生のパスワードからハッシュ化されたパスワードを生成する
     *
     * @param rawPassword 平文パスワード
     * @return Password インスタンス
     * @throws IllegalArgumentException パスワードが不正な場合
     */
    public static Password fromRawPassword(String rawPassword) {
        validate(rawPassword);
        String hashed = PASSWORD_ENCODER.encode(rawPassword);
        return new Password(hashed);
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

    private static void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("パスワードは必須です");
        }
        if (rawPassword.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("パスワードは%d文字以上で入力してください", MIN_LENGTH));
        }
    }

    /**
     * 平文パスワードがこのパスワードと一致するかを検証する
     *
     * @param rawPassword 平文パスワード
     * @return 一致する場合 true
     */
    public boolean matches(String rawPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, this.hashedValue);
    }

    /**
     * ハッシュ化されたパスワードを取得する（DB 保存用）
     *
     * @return ハッシュ化されたパスワード
     */
    public String getValue() {
        return hashedValue;
    }

    @Override
    public String toString() {
        return "********";
    }
}
