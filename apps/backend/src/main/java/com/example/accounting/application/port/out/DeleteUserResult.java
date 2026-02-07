package com.example.accounting.application.port.out;

/**
 * ユーザー削除結果
 *
 * @param success      削除成功かどうか
 * @param errorMessage エラーメッセージ（失敗時のみ）
 */
public record DeleteUserResult(
        boolean success,
        String errorMessage
) {
    /**
     * 削除成功結果を生成する
     */
    public static DeleteUserResult ofSuccess() {
        return new DeleteUserResult(true, null);
    }

    /**
     * 削除失敗結果を生成する
     */
    public static DeleteUserResult failure(String message) {
        return new DeleteUserResult(false, message);
    }
}
