package com.example.accounting.presentation.api.auth;

/**
 * ログインレスポンス
 *
 * @param success      成功フラグ
 * @param accessToken  アクセストークン
 * @param refreshToken リフレッシュトークン
 * @param username     ユーザー名
 * @param role         ロール
 * @param errorMessage エラーメッセージ
 */
public record LoginResponse(
        boolean success,
        String accessToken,
        String refreshToken,
        String username,
        String role,
        String errorMessage
) {
    /**
     * 成功レスポンスを生成
     */
    public static LoginResponse success(String accessToken, String refreshToken, String username, String role) {
        return new LoginResponse(true, accessToken, refreshToken, username, role, null);
    }

    /**
     * 失敗レスポンスを生成
     */
    public static LoginResponse failure(String errorMessage) {
        return new LoginResponse(false, null, null, null, null, errorMessage);
    }
}
