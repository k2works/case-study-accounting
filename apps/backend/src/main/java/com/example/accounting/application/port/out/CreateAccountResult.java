package com.example.accounting.application.port.out;

/**
 * 勘定科目登録結果
 *
 * @param success      登録成功かどうか
 * @param accountId    勘定科目ID（成功時のみ）
 * @param accountCode  勘定科目コード（成功時のみ）
 * @param accountName  勘定科目名（成功時のみ）
 * @param accountType  勘定科目種別（成功時のみ）
 * @param errorMessage エラーメッセージ（失敗時のみ）
 */
public record CreateAccountResult(
        boolean success,
        Integer accountId,
        String accountCode,
        String accountName,
        String accountType,
        String errorMessage
) {
    /**
     * 登録成功結果を生成する
     */
    public static CreateAccountResult success(
            Integer accountId,
            String accountCode,
            String accountName,
            String accountType
    ) {
        return new CreateAccountResult(true, accountId, accountCode, accountName, accountType, null);
    }

    /**
     * 登録失敗結果を生成する
     */
    public static CreateAccountResult failure(String errorMessage) {
        return new CreateAccountResult(false, null, null, null, null, errorMessage);
    }
}
