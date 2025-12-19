package com.example.accounting.presentation.api.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * ログインリクエスト
 *
 * @param username ユーザー名
 * @param password パスワード
 */
public record LoginRequest(
        @NotBlank(message = "ユーザー名を入力してください")
        String username,
        @NotBlank(message = "パスワードを入力してください")
        String password
) {
}
