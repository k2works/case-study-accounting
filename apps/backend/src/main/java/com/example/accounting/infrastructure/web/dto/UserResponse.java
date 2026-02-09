package com.example.accounting.infrastructure.web.dto;

import com.example.accounting.application.port.in.query.UserSummary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * ユーザー一覧レスポンス
 *
 * @param id          ユーザーID
 * @param username    ユーザー名
 * @param email       メールアドレス
 * @param displayName 表示名
 * @param role        ロール
 * @param lastLoginAt 最終ログイン日時
 */
@Schema(description = "ユーザー一覧レスポンス")
public record UserResponse(
        @Schema(description = "ユーザーID", example = "user-1")
        String id,

        @Schema(description = "ユーザー名", example = "user")
        String username,

        @Schema(description = "メールアドレス", example = "user@example.com")
        String email,

        @Schema(description = "表示名", example = "表示ユーザー")
        String displayName,

        @Schema(description = "ユーザーロール（ADMIN, MANAGER, USER, VIEWER）", example = "USER")
        String role,

        @Schema(description = "最終ログイン日時", example = "2024-01-01T10:00:00")
        LocalDateTime lastLoginAt
) {
    public static UserResponse from(UserSummary summary) {
        return new UserResponse(
                summary.id(),
                summary.username(),
                summary.email(),
                summary.displayName(),
                summary.role(),
                summary.lastLoginAt()
        );
    }
}
