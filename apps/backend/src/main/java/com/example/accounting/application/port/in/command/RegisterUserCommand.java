package com.example.accounting.application.port.in.command;

import io.vavr.control.Either;

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
    private static final int MIN_PASSWORD_LENGTH = 8;

    public static Either<String, RegisterUserCommand> of(
            String username,
            String email,
            String password,
            String displayName,
            String role
    ) {
        return requireNonBlank(username, "ユーザー名は必須です")
                .flatMap(v -> requireNonBlank(email, "メールアドレスは必須です"))
                .flatMap(v -> validatePassword(password))
                .flatMap(v -> requireNonBlank(displayName, "表示名は必須です"))
                .flatMap(v -> requireNonBlank(role, "ロールは必須です"))
                .map(v -> new RegisterUserCommand(username, email, password, displayName, role));
    }

    private static Either<String, Void> validatePassword(String password) {
        if (password == null || password.isBlank()) {
            return Either.left("パスワードは必須です");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return Either.left("パスワードは 8 文字以上で入力してください");
        }
        return Either.right(null);
    }

    private static Either<String, Void> requireNonBlank(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            return Either.left(errorMessage);
        }
        return Either.right(null);
    }
}
