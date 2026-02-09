package com.example.accounting.application.port.in.command;

/**
 * ユーザー削除コマンド
 *
 * @param userId ユーザーID
 */
public record DeleteUserCommand(String userId) {
    public DeleteUserCommand {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("ユーザーIDは必須です");
        }
    }
}
