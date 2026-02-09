package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * ユーザー更新レスポンス
 *
 * @param success      成功フラグ
 * @param id           ユーザーID
 * @param username     ユーザー名
 * @param email        メールアドレス
 * @param displayName  表示名
 * @param role         ロール
 * @param errorMessage エラーメッセージ
 */
@Schema(description = "ユーザー更新レスポンス")
public record UpdateUserResponse(
        @Schema(description = "更新成功フラグ", example = "true")
        boolean success,

        @Schema(description = "ユーザーID（更新成功時のみ）", example = "user-1")
        String id,

        @Schema(description = "ユーザー名（更新成功時のみ）", example = "user")
        String username,

        @Schema(description = "メールアドレス（更新成功時のみ）", example = "user@example.com")
        String email,

        @Schema(description = "表示名（更新成功時のみ）", example = "更新ユーザー")
        String displayName,

        @Schema(description = "ユーザーロール（ADMIN, MANAGER, USER, VIEWER）", example = "USER")
        String role,

        @Schema(description = "エラーメッセージ（更新失敗時のみ）", example = "ユーザーが見つかりません")
        String errorMessage
) {
    /**
     * 成功レスポンスを生成
     */
    public static UpdateUserResponse success(
            String id,
            String username,
            String email,
            String displayName,
            String role
    ) {
        return new UpdateUserResponse(true, id, username, email, displayName, role, null);
    }

    /**
     * 失敗レスポンスを生成
     */
    public static UpdateUserResponse failure(String errorMessage) {
        return new UpdateUserResponse(false, null, null, null, null, null, errorMessage);
    }
}
