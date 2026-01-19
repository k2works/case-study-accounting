package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 勘定科目登録リクエスト
 *
 * @param accountCode 勘定科目コード
 * @param accountName 勘定科目名
 * @param accountType 勘定科目種別
 */
@Schema(description = "勘定科目登録リクエスト")
public record CreateAccountRequest(
        @Schema(description = "勘定科目コード", example = "1100", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "勘定科目コードを入力してください")
        @Pattern(regexp = "\\d{4}", message = "勘定科目コードは4桁の数字です")
        String accountCode,

        @Schema(description = "勘定科目名", example = "現金", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "勘定科目名を入力してください")
        String accountName,

        @Schema(description = "勘定科目種別（ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE）", example = "ASSET",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "勘定科目種別を選択してください")
        String accountType
) {
}
