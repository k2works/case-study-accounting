package com.example.accounting.application.port.in.command;

import io.vavr.control.Either;

/**
 * ユーザー更新コマンド
 *
 * @param userId      ユーザーID
 * @param displayName 表示名
 * @param password    パスワード（任意）
 * @param role        ロール
 */
public record UpdateUserCommand(
        String userId,
        String displayName,
        String password,
        String role
) {

    public static Either<String, UpdateUserCommand> of(
            String userId,
            String displayName,
            String password,
            String role
    ) {
        if (userId == null || userId.isBlank()) {
            return Either.left("ユーザーIDは必須です");
        }
        if (displayName == null || displayName.isBlank()) {
            return Either.left("表示名は必須です");
        }
        if (role == null || role.isBlank()) {
            return Either.left("ロールは必須です");
        }
        return Either.right(new UpdateUserCommand(userId, displayName, password, role));
    }
}
