package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "ログインレスポンス")
public record LoginResponse(
        @Schema(description = "認証成功フラグ", example = "true")
        boolean success,

        @Schema(description = "JWT アクセストークン（認証成功時のみ）", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "JWT リフレッシュトークン（認証成功時のみ）", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken,

        @Schema(description = "ユーザー名（認証成功時のみ）", example = "admin")
        String username,

        @Schema(description = "ユーザーロール（ADMIN, MANAGER, USER, VIEWER）", example = "ADMIN")
        String role,

        @Schema(description = "エラーメッセージ（認証失敗時のみ）", example = "ユーザー名またはパスワードが正しくありません")
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
