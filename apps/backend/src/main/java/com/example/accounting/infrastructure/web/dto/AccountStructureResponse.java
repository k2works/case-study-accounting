package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "勘定科目構成レスポンス")
public record AccountStructureResponse(
        @Schema(description = "勘定科目コード", example = "1100")
        String accountCode,

        @Schema(description = "勘定科目名", example = "現金")
        String accountName,

        @Schema(description = "勘定科目パス", example = "1000~1100")
        String accountPath,

        @Schema(description = "階層レベル", example = "2")
        int hierarchyLevel,

        @Schema(description = "親勘定科目コード", example = "1000")
        String parentAccountCode,

        @Schema(description = "表示順", example = "10")
        int displayOrder
) {
}
