package com.example.accounting.application.port.out;

/**
 * ユーザー登録結果
 *
 * @param success      登録成功かどうか
 * @param username     ユーザー名（成功時のみ）
 * @param email        メールアドレス（成功時のみ）
 * @param displayName  表示名（成功時のみ）
 * @param role         ロール（成功時のみ）
 * @param errorMessage エラーメッセージ（失敗時のみ）
 */
public record RegisterUserResult(
        boolean success,
        String username,
        String email,
        String displayName,
        String role,
        String errorMessage
) {
    /**
     * 登録成功結果を生成する
     */
    public static RegisterUserResult success(
            String username,
            String email,
            String displayName,
            String role
    ) {
        return new RegisterUserResult(true, username, email, displayName, role, null);
    }

    /**
     * 登録失敗結果を生成する
     */
    public static RegisterUserResult failure(String errorMessage) {
        return new RegisterUserResult(false, null, null, null, null, errorMessage);
    }
}
