package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "勘定科目構成更新リクエスト")
public record UpdateAccountStructureRequest(
        @Schema(description = "親勘定科目コード（ルートの場合は null）", example = "1000")
        String parentAccountCode,

        @Schema(description = "表示順", example = "20")
        int displayOrder
) {
}
