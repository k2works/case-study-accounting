package com.example.accounting.domain.model.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * ユーザーエンティティ
 *
 * <p>財務会計システムのユーザーを表すエンティティ。
 * 認証情報、権限、アカウント状態を管理する。</p>
 *
 * <p>イミュータブル設計により、状態変更は常に新しいインスタンスを返す。
 * これにより、並行処理での安全性と、変更履歴の追跡が容易になる。</p>
 *
 * <p>副作用（現在時刻取得、ID生成）はパラメータ化されており、
 * テスト時に固定値を注入可能。</p>
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

    // ===== ファクトリメソッド =====

    /**
     * ファクトリメソッド - 新規作成（現在時刻を使用）
     *
     * @param username    ユーザー名
     * @param email       メールアドレス
     * @param password    パスワード
     * @param displayName 表示名
     * @param role        ロール
     * @return 新しい User インスタンス
     */
    public static User create(Username username,
                              Email email,
                              Password password,
                              String displayName,
                              Role role) {
        return create(username, email, password, displayName, role,
                UserId::generate, LocalDateTime::now);
    }

    /**
     * ファクトリメソッド - 新規作成（ID生成と時刻を注入可能）
     *
     * <p>テスト時に固定の ID と時刻を注入するために使用。</p>
     *
     * @param username       ユーザー名
     * @param email          メールアドレス
     * @param password       パスワード
     * @param displayName    表示名
     * @param role           ロール
     * @param idSupplier     ID 生成関数
     * @param clockSupplier  現在時刻取得関数
     * @return 新しい User インスタンス
     * @throws NullPointerException 引数が null の場合
     */
    public static User create(Username username,
                              Email email,
                              Password password,
                              String displayName,
                              Role role,
                              Supplier<UserId> idSupplier,
                              Supplier<LocalDateTime> clockSupplier) {
        Objects.requireNonNull(username, "ユーザー名は必須です");
        Objects.requireNonNull(email, "メールアドレスは必須です");
        Objects.requireNonNull(password, "パスワードは必須です");
        Objects.requireNonNull(displayName, "表示名は必須です");
        Objects.requireNonNull(role, "ロールは必須です");
        Objects.requireNonNull(idSupplier, "ID生成関数は必須です");
        Objects.requireNonNull(clockSupplier, "時刻取得関数は必須です");

        LocalDateTime now = clockSupplier.get();

        return new User(
                idSupplier.get(),
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
        return username.value();
    }

    /**
     * メールアドレスを文字列で取得する
     *
     * @return メールアドレス
     */
    public String getEmailValue() {
        return email.value();
    }

    /**
     * ハッシュ化されたパスワードを取得する（DB 保存用）
     *
     * @return ハッシュ化されたパスワード
     */
    public String getPasswordValue() {
        return password.value();
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

    // ===== 状態変更メソッド（純粋関数版 - 時刻をパラメータで受け取る） =====

    /**
     * ログイン失敗を記録した新しいインスタンスを返す（時刻指定）
     *
     * @param now 現在時刻
     * @return 更新された User インスタンス
     */
    public User recordFailedLoginAttemptAt(LocalDateTime now) {
        int newFailedAttempts = this.failedLoginAttempts + 1;
        boolean newLocked = newFailedAttempts >= MAX_FAILED_ATTEMPTS || this.locked;
        return this
                .withFailedLoginAttempts(newFailedAttempts)
                .withLocked(newLocked)
                .withUpdatedAt(now);
    }

    /**
     * ログイン成功を記録した新しいインスタンスを返す（時刻指定）
     *
     * @param now 現在時刻
     * @return 更新された User インスタンス
     */
    public User recordSuccessfulLoginAt(LocalDateTime now) {
        return this
                .withFailedLoginAttempts(0)
                .withLastLoginAt(now)
                .withUpdatedAt(now);
    }

    /**
     * アカウントを無効化した新しいインスタンスを返す（時刻指定）
     *
     * @param now 現在時刻
     * @return 更新された User インスタンス
     */
    public User deactivateAt(LocalDateTime now) {
        return this
                .withActive(false)
                .withUpdatedAt(now);
    }

    /**
     * アカウントを有効化した新しいインスタンスを返す（時刻指定）
     *
     * @param now 現在時刻
     * @return 更新された User インスタンス
     */
    public User activateAt(LocalDateTime now) {
        return this
                .withActive(true)
                .withUpdatedAt(now);
    }

    /**
     * アカウントのロックを解除した新しいインスタンスを返す（時刻指定）
     *
     * @param now 現在時刻
     * @return 更新された User インスタンス
     */
    public User unlockAt(LocalDateTime now) {
        return this
                .withLocked(false)
                .withFailedLoginAttempts(0)
                .withUpdatedAt(now);
    }

    /**
     * パスワードを変更した新しいインスタンスを返す（時刻指定）
     *
     * @param newRawPassword 新しい平文パスワード
     * @param now            現在時刻
     * @return 更新された User インスタンス
     * @throws IllegalArgumentException パスワードが不正な場合
     */
    public User changePasswordAt(String newRawPassword, LocalDateTime now) {
        return this
                .withPassword(Password.fromRawPassword(newRawPassword))
                .withUpdatedAt(now);
    }

    /**
     * ロールを変更した新しいインスタンスを返す（時刻指定）
     *
     * @param newRole 新しいロール
     * @param now     現在時刻
     * @return 更新された User インスタンス
     * @throws NullPointerException ロールが null の場合
     */
    public User changeRoleAt(Role newRole, LocalDateTime now) {
        Objects.requireNonNull(newRole, "ロールは必須です");
        return this
                .withRole(newRole)
                .withUpdatedAt(now);
    }

    // ===== 状態変更メソッド（便利メソッド - 現在時刻を自動取得） =====

    /**
     * ログイン失敗を記録した新しいインスタンスを返す
     *
     * <p>失敗回数が上限に達した場合、アカウントをロックする。</p>
     *
     * @return 更新された User インスタンス
     */
    public User recordFailedLoginAttempt() {
        return recordFailedLoginAttemptAt(LocalDateTime.now());
    }

    /**
     * ログイン成功を記録した新しいインスタンスを返す
     *
     * <p>失敗回数をリセットし、最終ログイン日時を更新する。</p>
     *
     * @return 更新された User インスタンス
     */
    public User recordSuccessfulLogin() {
        return recordSuccessfulLoginAt(LocalDateTime.now());
    }

    /**
     * アカウントを無効化した新しいインスタンスを返す
     *
     * @return 更新された User インスタンス
     */
    public User deactivate() {
        return deactivateAt(LocalDateTime.now());
    }

    /**
     * アカウントを有効化した新しいインスタンスを返す
     *
     * @return 更新された User インスタンス
     */
    public User activate() {
        return activateAt(LocalDateTime.now());
    }

    /**
     * アカウントのロックを解除した新しいインスタンスを返す
     *
     * @return 更新された User インスタンス
     */
    public User unlock() {
        return unlockAt(LocalDateTime.now());
    }

    /**
     * パスワードを変更した新しいインスタンスを返す
     *
     * @param newRawPassword 新しい平文パスワード
     * @return 更新された User インスタンス
     * @throws IllegalArgumentException パスワードが不正な場合
     */
    public User changePassword(String newRawPassword) {
        return changePasswordAt(newRawPassword, LocalDateTime.now());
    }

    /**
     * ロールを変更した新しいインスタンスを返す
     *
     * @param newRole 新しいロール
     * @return 更新された User インスタンス
     * @throws NullPointerException ロールが null の場合
     */
    public User changeRole(Role newRole) {
        return changeRoleAt(newRole, LocalDateTime.now());
    }
}
