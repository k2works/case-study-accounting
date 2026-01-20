package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 勘定科目更新リクエスト
 *
 * @param accountName 勘定科目名
 * @param accountType 勘定科目種別
 */
@Schema(description = "勘定科目更新リクエスト")
public record UpdateAccountRequest(
        @Schema(description = "勘定科目名", example = "普通預金", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "勘定科目名を入力してください")
        String accountName,

        @Schema(description = "勘定科目種別（ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE）", example = "ASSET",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "勘定科目種別を選択してください")
        String accountType
) {
}
