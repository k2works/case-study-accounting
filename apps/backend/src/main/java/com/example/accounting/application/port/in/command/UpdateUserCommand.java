package com.example.accounting.application.port.in.command;

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
    public UpdateUserCommand {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("ユーザーIDは必須です");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("表示名は必須です");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("ロールは必須です");
        }
    }
}
