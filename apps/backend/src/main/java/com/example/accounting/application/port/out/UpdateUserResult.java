package com.example.accounting.application.port.out;

/**
 * ユーザー更新結果
 *
 * @param success      更新成功かどうか
 * @param id           ユーザーID（成功時のみ）
 * @param username     ユーザー名（成功時のみ）
 * @param email        メールアドレス（成功時のみ）
 * @param displayName  表示名（成功時のみ）
 * @param role         ロール（成功時のみ）
 * @param errorMessage エラーメッセージ（失敗時のみ）
 */
public record UpdateUserResult(
        boolean success,
        String id,
        String username,
        String email,
        String displayName,
        String role,
        String errorMessage
) {
    /**
     * 更新成功結果を生成する
     */
    public static UpdateUserResult success(
            String id,
            String username,
            String email,
            String displayName,
            String role
    ) {
        return new UpdateUserResult(true, id, username, email, displayName, role, null);
    }

    /**
     * 更新失敗結果を生成する
     */
    public static UpdateUserResult failure(String errorMessage) {
        return new UpdateUserResult(false, null, null, null, null, null, errorMessage);
    }
}
