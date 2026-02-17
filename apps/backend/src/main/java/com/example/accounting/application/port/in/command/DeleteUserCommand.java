package com.example.accounting.application.port.in.command;

import io.vavr.control.Either;

/**
 * ユーザー削除コマンド
 *
 * @param userId ユーザーID
 */
public record DeleteUserCommand(String userId) {

    public static Either<String, DeleteUserCommand> of(String userId) {
        if (userId == null || userId.isBlank()) {
            return Either.left("ユーザーIDは必須です");
        }
        return Either.right(new DeleteUserCommand(userId));
    }
}
