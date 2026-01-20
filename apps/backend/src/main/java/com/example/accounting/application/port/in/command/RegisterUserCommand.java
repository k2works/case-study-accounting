package com.example.accounting.application.port.in.command;

/**
 * ユーザー登録コマンド
 *
 * @param username    ユーザー名
 * @param email       メールアドレス
 * @param password    パスワード
 * @param displayName 表示名
 * @param role        ロール
 */
public record RegisterUserCommand(
        String username,
        String email,
        String password,
        String displayName,
        String role
) {
    public RegisterUserCommand {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("ユーザー名は必須です");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("メールアドレスは必須です");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("パスワードは必須です");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("パスワードは 8 文字以上で入力してください");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("表示名は必須です");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("ロールは必須です");
        }
    }
}
