package com.example.accounting.domain.model.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * ユーザーエンティティ
 *
 * <p>財務会計システムのユーザーを表すエンティティ。
 * 認証情報、権限、アカウント状態を管理する。</p>
 */
public class User {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final UserId id;
    private final String username;
    private final String email;
    private String password;
    private String displayName;
    private Role role;
    private boolean active;
    private boolean locked;
    private int failedLoginAttempts;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private User(UserId id,
                 String username,
                 String email,
                 String password,
                 String displayName,
                 Role role,
                 boolean active,
                 boolean locked,
                 int failedLoginAttempts,
                 LocalDateTime lastLoginAt,
                 LocalDateTime createdAt,
                 LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.displayName = displayName;
        this.role = role;
        this.active = active;
        this.locked = locked;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 新しいユーザーを作成するファクトリメソッド
     *
     * @param username    ユーザー名
     * @param email       メールアドレス
     * @param rawPassword 平文パスワード
     * @param displayName 表示名
     * @param role        ロール
     * @return 新しいユーザー
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
     * データベースからの再構築用ファクトリメソッド
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
     * ログイン失敗を記録する
     *
     * <p>失敗回数が上限に達した場合、アカウントをロックする。</p>
     */
    public void recordFailedLoginAttempt() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
            this.locked = true;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * ログイン成功を記録する
     *
     * <p>失敗回数をリセットし、最終ログイン日時を更新する。</p>
     */
    public void recordSuccessfulLogin() {
        this.failedLoginAttempts = 0;
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * ログイン可能かどうかを判定する
     *
     * @return ログイン可能な場合 true
     */
    public boolean canLogin() {
        return this.active && !this.locked;
    }

    /**
     * アカウントを無効化する
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * アカウントを有効化する
     */
    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * アカウントのロックを解除する
     */
    public void unlock() {
        this.locked = false;
        this.failedLoginAttempts = 0;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * パスワードを変更する
     *
     * @param newRawPassword 新しい平文パスワード
     */
    public void changePassword(String newRawPassword) {
        validatePassword(newRawPassword);
        this.password = PASSWORD_ENCODER.encode(newRawPassword);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * ロールを変更する
     *
     * @param newRole 新しいロール
     */
    public void changeRole(Role newRole) {
        Objects.requireNonNull(newRole, "ロールは必須です");
        this.role = newRole;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public UserId getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isLocked() {
        return locked;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
