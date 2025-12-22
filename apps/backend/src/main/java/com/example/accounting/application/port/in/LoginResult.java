package com.example.accounting.application.port.in;

import com.example.accounting.domain.model.user.Role;

/**
 * ログイン結果
 *
 * @param success      ログイン成功かどうか
 * @param accessToken  アクセストークン（成功時のみ）
 * @param refreshToken リフレッシュトークン（成功時のみ）
 * @param username     ユーザー名（成功時のみ）
 * @param role         ロール（成功時のみ）
 * @param errorMessage エラーメッセージ（失敗時のみ）
 */
public record LoginResult(
        boolean success,
        String accessToken,
        String refreshToken,
        String username,
        Role role,
        String errorMessage
) {
    /**
     * ログイン成功結果を生成する
     */
    public static LoginResult success(String accessToken, String refreshToken, String username, Role role) {
        return new LoginResult(true, accessToken, refreshToken, username, role, null);
    }

    /**
     * ログイン失敗結果を生成する
     */
    public static LoginResult failure(String errorMessage) {
        return new LoginResult(false, null, null, null, null, errorMessage);
    }
}
