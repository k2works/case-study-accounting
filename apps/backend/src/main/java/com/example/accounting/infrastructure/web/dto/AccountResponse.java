package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 勘定科目レスポンス
 *
 * @param accountId   勘定科目ID
 * @param accountCode 勘定科目コード
 * @param accountName 勘定科目名
 * @param accountType 勘定科目種別
 */
@Schema(description = "勘定科目レスポンス")
public record AccountResponse(
        @Schema(description = "勘定科目ID", example = "1")
        Integer accountId,

        @Schema(description = "勘定科目コード", example = "1100")
        String accountCode,

        @Schema(description = "勘定科目名", example = "現金")
        String accountName,

        @Schema(description = "勘定科目種別（ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE）", example = "ASSET")
        String accountType
) {
}
