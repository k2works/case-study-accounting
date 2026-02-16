package com.example.accounting.application.port.in.command;

import io.vavr.control.Either;

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

    public static Either<String, LoginCommand> of(String username, String password) {
        if (username == null || username.isBlank()) {
            return Either.left("ユーザー名は必須です");
        }
        if (password == null || password.isBlank()) {
            return Either.left("パスワードは必須です");
        }
        return Either.right(new LoginCommand(username, password));
    }
}
