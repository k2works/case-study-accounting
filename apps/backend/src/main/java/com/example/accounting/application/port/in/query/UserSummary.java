package com.example.accounting.application.port.in.query;

import com.example.accounting.domain.model.user.User;

import java.time.LocalDateTime;

/**
 * ユーザー一覧表示用 DTO
 *
 * @param id          ユーザーID
 * @param username    ユーザー名
 * @param email       メールアドレス
 * @param displayName 表示名
 * @param role        ロール
 * @param lastLoginAt 最終ログイン日時
 */
public record UserSummary(
        String id,
        String username,
        String email,
        String displayName,
        String role,
        LocalDateTime lastLoginAt
) {
    public static UserSummary from(User user) {
        return new UserSummary(
                user.getId().value(),
                user.getUsername().value(),
                user.getEmail().value(),
                user.getDisplayName(),
                user.getRole().name(),
                user.getLastLoginAt()
        );
    }
}
