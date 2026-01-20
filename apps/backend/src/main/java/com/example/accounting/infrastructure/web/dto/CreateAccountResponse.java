package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 勘定科目登録レスポンス
 *
 * @param success      成功フラグ
 * @param accountId    勘定科目ID
 * @param accountCode  勘定科目コード
 * @param accountName  勘定科目名
 * @param accountType  勘定科目種別
 * @param errorMessage エラーメッセージ
 */
@Schema(description = "勘定科目登録レスポンス")
public record CreateAccountResponse(
        @Schema(description = "登録成功フラグ", example = "true")
        boolean success,

        @Schema(description = "勘定科目ID（登録成功時のみ）", example = "1")
        Integer accountId,

        @Schema(description = "勘定科目コード（登録成功時のみ）", example = "1100")
        String accountCode,

        @Schema(description = "勘定科目名（登録成功時のみ）", example = "現金")
        String accountName,

        @Schema(description = "勘定科目種別（ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE）", example = "ASSET")
        String accountType,

        @Schema(description = "エラーメッセージ（登録失敗時のみ）", example = "勘定科目コードは既に使用されています")
        String errorMessage
) {
    /**
     * 成功レスポンスを生成
     */
    public static CreateAccountResponse success(
            Integer accountId,
            String accountCode,
            String accountName,
            String accountType
    ) {
        return new CreateAccountResponse(true, accountId, accountCode, accountName, accountType, null);
    }

    /**
     * 失敗レスポンスを生成
     */
    public static CreateAccountResponse failure(String errorMessage) {
        return new CreateAccountResponse(false, null, null, null, null, errorMessage);
    }
}
