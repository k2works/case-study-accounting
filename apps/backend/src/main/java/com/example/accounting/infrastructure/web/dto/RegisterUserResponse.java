package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * ユーザー登録レスポンス
 *
 * @param success      成功フラグ
 * @param username     ユーザー名
 * @param email        メールアドレス
 * @param displayName  表示名
 * @param role         ロール
 * @param errorMessage エラーメッセージ
 */
@Schema(description = "ユーザー登録レスポンス")
public record RegisterUserResponse(
        @Schema(description = "登録成功フラグ", example = "true")
        boolean success,

        @Schema(description = "ユーザー名（登録成功時のみ）", example = "newuser")
        String username,

        @Schema(description = "メールアドレス（登録成功時のみ）", example = "newuser@example.com")
        String email,

        @Schema(description = "表示名（登録成功時のみ）", example = "新規ユーザー")
        String displayName,

        @Schema(description = "ユーザーロール（ADMIN, MANAGER, USER, VIEWER）", example = "USER")
        String role,

        @Schema(description = "エラーメッセージ（登録失敗時のみ）", example = "ユーザー名は既に使用されています")
        String errorMessage
) {
    /**
     * 成功レスポンスを生成
     */
    public static RegisterUserResponse success(
            String username,
            String email,
            String displayName,
            String role
    ) {
        return new RegisterUserResponse(true, username, email, displayName, role, null);
    }

    /**
     * 失敗レスポンスを生成
     */
    public static RegisterUserResponse failure(String errorMessage) {
        return new RegisterUserResponse(false, null, null, null, null, errorMessage);
    }
}
