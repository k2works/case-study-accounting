package com.example.accounting.application.port.in.query;

/**
 * ユーザー一覧取得クエリ
 *
 * @param role    ロール（ADMIN, MANAGER, USER, VIEWER、null で全件）
 * @param keyword ユーザーID または氏名での検索（null で全件）
 */
public record GetUsersQuery(
        String role,
        String keyword
) {
    public static GetUsersQuery all() {
        return new GetUsersQuery(null, null);
    }

    public static GetUsersQuery byRole(String role) {
        return new GetUsersQuery(role, null);
    }

    public static GetUsersQuery byKeyword(String keyword) {
        return new GetUsersQuery(null, keyword);
    }
}
