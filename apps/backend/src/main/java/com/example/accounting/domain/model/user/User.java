package com.example.accounting.domain.model.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * ユーザーエンティティ
 *
 * <p>財務会計システムのユーザーを表すエンティティ。
 * 認証情報、権限、アカウント状態を管理する。</p>
 *
 * <p>イミュータブル設計により、状態変更は常に新しいインスタンスを返す。
 * これにより、並行処理での安全性と、変更履歴の追跡が容易になる。</p>
 */
@Value
@With
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    UserId id;
    String username;
    String email;
    String password;
    String displayName;
    Role role;
    boolean active;
    boolean locked;
    int failedLoginAttempts;
    LocalDateTime lastLoginAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    /**
     * ファクトリメソッド - 新規作成
     *
     * <p>新しいユーザーを作成する。ID は自動生成され、パスワードはハッシュ化される。
     * すべての入力値に対してバリデーションを実行する。</p>
     *
     * @param username    ユーザー名（3〜50文字）
     * @param email       メールアドレス
     * @param rawPassword 平文パスワード（8文字以上）
     * @param displayName 表示名
     * @param role        ロール
     * @return 新しい User インスタンス
     * @throws IllegalArgumentException 入力値が不正な場合
     */
    public static User create(String username,
                              String email,
                              String rawPassword,
                              String displayName,
                              Role role) {
        validateUsername(username);
        validateEmail(email);
        validatePassword(rawPassword);
        Objects.requireNonNull(displayName, "表示名は必須です");
        Objects.requireNonNull(role, "ロールは必須です");

        LocalDateTime now = LocalDateTime.now();
        String hashedPassword = PASSWORD_ENCODER.encode(rawPassword);

        return new User(
                UserId.generate(),
                username,
                email,
                hashedPassword,
                displayName,
                role,
                true,  // active
                false, // locked
                0,     // failedLoginAttempts
                null,  // lastLoginAt
                now,   // createdAt
                now    // updatedAt
        );
    }

    /**
     * 再構築用ファクトリメソッド - DB からの復元
     *
     * <p>データベースから読み込んだデータを使ってエンティティを再構築する。
     * バリデーションはスキップされる（DB に保存されているデータは既に検証済みのため）。</p>
     *
     * @param id                  ユーザー ID
     * @param username            ユーザー名
     * @param email               メールアドレス
     * @param hashedPassword      ハッシュ化されたパスワード
     * @param displayName         表示名
     * @param role                ロール
     * @param active              有効フラグ
     * @param locked              ロックフラグ
     * @param failedLoginAttempts ログイン失敗回数
     * @param lastLoginAt         最終ログイン日時
     * @param createdAt           作成日時
     * @param updatedAt           更新日時
     * @return 再構築された User インスタンス
     */
    public static User reconstruct(UserId id,
                                   String username,
                                   String email,
                                   String hashedPassword,
                                   String displayName,
                                   Role role,
                                   boolean active,
                                   boolean locked,
                                   int failedLoginAttempts,
                                   LocalDateTime lastLoginAt,
                                   LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {
        return new User(id, username, email, hashedPassword, displayName, role,
                active, locked, failedLoginAttempts, lastLoginAt, createdAt, updatedAt);
    }

    // ===== バリデーション =====

    private static void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("ユーザー名は必須です");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("ユーザー名は3〜50文字で入力してください");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("メールアドレスは必須です");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("メールアドレスの形式が不正です");
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("パスワードは必須です");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("パスワードは8文字以上で入力してください");
        }
    }

    // ===== クエリメソッド =====

    /**
     * パスワードを検証する
     *
     * @param rawPassword 平文パスワード
     * @return 一致する場合 true
     */
    public boolean verifyPassword(String rawPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, this.password);
    }

    /**
     * ログイン可能かどうかを判定する
     *
     * @return ログイン可能な場合 true
     */
    public boolean canLogin() {
        return this.active && !this.locked;
    }

    // ===== 状態変更メソッド（@With を活用して新しいインスタンスを返す） =====

    /**
     * ログイン失敗を記録した新しいインスタンスを返す
     *
     * <p>失敗回数が上限に達した場合、アカウントをロックする。</p>
     *
     * @return 更新された User インスタンス
     */
    public User recordFailedLoginAttempt() {
        int newFailedAttempts = this.failedLoginAttempts + 1;
        boolean newLocked = newFailedAttempts >= MAX_FAILED_ATTEMPTS || this.locked;
        return this
                .withFailedLoginAttempts(newFailedAttempts)
                .withLocked(newLocked)
                .withUpdatedAt(LocalDateTime.now());
    }

    /**
     * ログイン成功を記録した新しいインスタンスを返す
     *
     * <p>失敗回数をリセットし、最終ログイン日時を更新する。</p>
     *
     * @return 更新された User インスタンス
     */
    public User recordSuccessfulLogin() {
        LocalDateTime now = LocalDateTime.now();
        return this
                .withFailedLoginAttempts(0)
                .withLastLoginAt(now)
                .withUpdatedAt(now);
    }

    /**
     * アカウントを無効化した新しいインスタンスを返す
     *
     * @return 更新された User インスタンス
     */
    public User deactivate() {
        return this
                .withActive(false)
                .withUpdatedAt(LocalDateTime.now());
    }

    /**
     * アカウントを有効化した新しいインスタンスを返す
     *
     * @return 更新された User インスタンス
     */
    public User activate() {
        return this
                .withActive(true)
                .withUpdatedAt(LocalDateTime.now());
    }

    /**
     * アカウントのロックを解除した新しいインスタンスを返す
     *
     * @return 更新された User インスタンス
     */
    public User unlock() {
        return this
                .withLocked(false)
                .withFailedLoginAttempts(0)
                .withUpdatedAt(LocalDateTime.now());
    }

    /**
     * パスワードを変更した新しいインスタンスを返す
     *
     * @param newRawPassword 新しい平文パスワード
     * @return 更新された User インスタンス
     * @throws IllegalArgumentException パスワードが不正な場合
     */
    public User changePassword(String newRawPassword) {
        validatePassword(newRawPassword);
        return this
                .withPassword(PASSWORD_ENCODER.encode(newRawPassword))
                .withUpdatedAt(LocalDateTime.now());
    }

    /**
     * ロールを変更した新しいインスタンスを返す
     *
     * @param newRole 新しいロール
     * @return 更新された User インスタンス
     * @throws NullPointerException ロールが null の場合
     */
    public User changeRole(Role newRole) {
        Objects.requireNonNull(newRole, "ロールは必須です");
        return this
                .withRole(newRole)
                .withUpdatedAt(LocalDateTime.now());
    }
}
