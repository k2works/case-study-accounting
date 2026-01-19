package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ユーザー登録リクエスト
 *
 * @param username    ユーザー名
 * @param email       メールアドレス
 * @param password    パスワード
 * @param displayName 表示名
 * @param role        ロール
 */
@Schema(description = "ユーザー登録リクエスト")
public record RegisterUserRequest(
        @Schema(description = "ユーザー名", example = "newuser", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "ユーザー名を入力してください")
        String username,

        @Schema(description = "メールアドレス", example = "newuser@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "メールアドレスを入力してください")
        @Email(message = "メールアドレスの形式が不正です")
        String email,

        @Schema(description = "パスワード", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "パスワードを入力してください")
        @Size(min = 8, message = "パスワードは 8 文字以上で入力してください")
        String password,

        @Schema(description = "表示名", example = "新規ユーザー", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "表示名を入力してください")
        String displayName,

        @Schema(description = "ユーザーロール（ADMIN, MANAGER, USER, VIEWER）", example = "USER",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "ロールを入力してください")
        String role
) {
}
