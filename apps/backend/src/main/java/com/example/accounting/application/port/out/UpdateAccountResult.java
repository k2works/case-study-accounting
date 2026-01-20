package com.example.accounting.application.port.out;

/**
 * 勘定科目更新結果
 *
 * @param success      更新成功かどうか
 * @param accountId    勘定科目ID（成功時のみ）
 * @param accountCode  勘定科目コード（成功時のみ）
 * @param accountName  勘定科目名（成功時のみ）
 * @param accountType  勘定科目種別（成功時のみ）
 * @param message      確認メッセージ（成功時のみ）
 * @param errorMessage エラーメッセージ（失敗時のみ）
 */
public record UpdateAccountResult(
        boolean success,
        Integer accountId,
        String accountCode,
        String accountName,
        String accountType,
        String message,
        String errorMessage
) {
    /**
     * 更新成功結果を生成する
     */
    public static UpdateAccountResult success(
            Integer accountId,
            String accountCode,
            String accountName,
            String accountType,
            String message
    ) {
        return new UpdateAccountResult(true, accountId, accountCode, accountName, accountType, message, null);
    }

    /**
     * 更新失敗結果を生成する
     */
    public static UpdateAccountResult failure(String errorMessage) {
        return new UpdateAccountResult(false, null, null, null, null, null, errorMessage);
    }
}
