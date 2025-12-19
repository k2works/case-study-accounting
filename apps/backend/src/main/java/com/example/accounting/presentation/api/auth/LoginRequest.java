package com.example.accounting.presentation.api.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * ログインリクエスト
 *
 * @param username ユーザー名
 * @param password パスワード
 */
@Schema(description = "ログインリクエスト")
public record LoginRequest(
        @Schema(description = "ユーザー名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "ユーザー名を入力してください")
        String username,

        @Schema(description = "パスワード", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "パスワードを入力してください")
        String password
) {
}
