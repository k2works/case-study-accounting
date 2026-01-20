package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 勘定科目更新レスポンス
 *
 * @param success      成功フラグ
 * @param accountId    勘定科目ID
 * @param accountCode  勘定科目コード
 * @param accountName  勘定科目名
 * @param accountType  勘定科目種別
 * @param message      確認メッセージ
 * @param errorMessage エラーメッセージ
 */
@Schema(description = "勘定科目更新レスポンス")
public record UpdateAccountResponse(
        @Schema(description = "更新成功フラグ", example = "true")
        boolean success,

        @Schema(description = "勘定科目ID（更新成功時のみ）", example = "1")
        Integer accountId,

        @Schema(description = "勘定科目コード（更新成功時のみ）", example = "1100")
        String accountCode,

        @Schema(description = "勘定科目名（更新成功時のみ）", example = "普通預金")
        String accountName,

        @Schema(description = "勘定科目種別（ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE）", example = "ASSET")
        String accountType,

        @Schema(description = "確認メッセージ（更新成功時のみ）", example = "勘定科目を更新しました")
        String message,

        @Schema(description = "エラーメッセージ（更新失敗時のみ）", example = "勘定科目が見つかりません")
        String errorMessage
) {
    /**
     * 成功レスポンスを生成
     */
    public static UpdateAccountResponse success(
            Integer accountId,
            String accountCode,
            String accountName,
            String accountType,
            String message
    ) {
        return new UpdateAccountResponse(true, accountId, accountCode, accountName, accountType, message, null);
    }

    /**
     * 失敗レスポンスを生成
     */
    public static UpdateAccountResponse failure(String errorMessage) {
        return new UpdateAccountResponse(false, null, null, null, null, null, errorMessage);
    }
}
