package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "勘定科目構成登録リクエスト")
public record CreateAccountStructureRequest(
        @Schema(description = "勘定科目コード", example = "1100", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "勘定科目コードを入力してください")
        String accountCode,

        @Schema(description = "親勘定科目コード（ルートの場合は null）", example = "1000")
        String parentAccountCode,

        @Schema(description = "表示順", example = "0")
        int displayOrder
) {
}
