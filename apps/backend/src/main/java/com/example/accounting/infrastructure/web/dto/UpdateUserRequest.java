package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * ユーザー更新リクエスト
 *
 * @param displayName 表示名
 * @param password    パスワード（任意）
 * @param role        ロール
 */
@Schema(description = "ユーザー更新リクエスト")
public record UpdateUserRequest(
        @Schema(description = "表示名", example = "更新ユーザー", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "表示名を入力してください")
        String displayName,

        @Schema(description = "パスワード（変更する場合のみ）", example = "Password123!",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String password,

        @Schema(description = "ユーザーロール（ADMIN, MANAGER, USER, VIEWER）", example = "USER",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "ロールを入力してください")
        String role
) {
}
