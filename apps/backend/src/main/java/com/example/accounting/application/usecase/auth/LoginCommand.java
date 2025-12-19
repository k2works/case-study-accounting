package com.example.accounting.application.usecase.auth;

/**
 * ログインコマンド
 *
 * @param username ユーザー名
 * @param password パスワード
 */
public record LoginCommand(
        String username,
        String password
) {
    public LoginCommand {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("ユーザー名は必須です");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("パスワードは必須です");
        }
    }
}
