package com.example.accounting.domain.model.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;
import java.util.Objects;

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

    UserId id;
    Username username;
    Email email;
    Password password;
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
     * <p>新しいユーザーを作成する。ID は自動生成される。</p>
     *
     * @param username    ユーザー名
     * @param email       メールアドレス
     * @param password    パスワード
     * @param displayName 表示名
     * @param role        ロール
     * @return 新しい User インスタンス
     * @throws NullPointerException 引数が null の場合
     */
    public static User create(Username username,
                              Email email,
                              Password password,
                              String displayName,
                              Role role) {
        Objects.requireNonNull(username, "ユーザー名は必須です");
        Objects.requireNonNull(email, "メールアドレスは必須です");
        Objects.requireNonNull(password, "パスワードは必須です");
        Objects.requireNonNull(displayName, "表示名は必須です");
        Objects.requireNonNull(role, "ロールは必須です");

        LocalDateTime now = LocalDateTime.now();

        return new User(
                UserId.generate(),
                username,
                email,
                password,
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
     * <p>データベースから読み込んだデータを使ってエンティティを再構築する。</p>
     *
     * @param id                  ユーザー ID
     * @param username            ユーザー名
     * @param email               メールアドレス
     * @param password            パスワード
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
                                   Username username,
                                   Email email,
                                   Password password,
                                   String displayName,
                                   Role role,
                                   boolean active,
                                   boolean locked,
                                   int failedLoginAttempts,
                                   LocalDateTime lastLoginAt,
                                   LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {
        return new User(
                id,
                username,
                email,
                password,
                displayName,
                role,
                active,
                locked,
                failedLoginAttempts,
                lastLoginAt,
                createdAt,
                updatedAt
        );
    }

    // ===== クエリメソッド =====

    /**
     * ユーザー名を文字列で取得する
     *
     * @return ユーザー名
     */
    public String getUsernameValue() {
        return username.getValue();
    }

    /**
     * メールアドレスを文字列で取得する
     *
     * @return メールアドレス
     */
    public String getEmailValue() {
        return email.getValue();
    }

    /**
     * ハッシュ化されたパスワードを取得する（DB 保存用）
     *
     * @return ハッシュ化されたパスワード
     */
    public String getPasswordValue() {
        return password.getValue();
    }

    /**
     * パスワードを検証する
     *
     * @param rawPassword 平文パスワード
     * @return 一致する場合 true
     */
    public boolean verifyPassword(String rawPassword) {
        return password.matches(rawPassword);
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
        return this
                .withPassword(Password.fromRawPassword(newRawPassword))
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
