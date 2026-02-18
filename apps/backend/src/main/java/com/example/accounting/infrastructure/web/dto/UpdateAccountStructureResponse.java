package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "勘定科目構成更新レスポンス")
public record UpdateAccountStructureResponse(
        @Schema(description = "更新成功フラグ", example = "true")
        boolean success,

        @Schema(description = "勘定科目コード", example = "1100")
        String accountCode,

        @Schema(description = "勘定科目パス", example = "1000~1100")
        String accountPath,

        @Schema(description = "階層レベル", example = "2")
        int hierarchyLevel,

        @Schema(description = "親勘定科目コード", example = "1000")
        String parentAccountCode,

        @Schema(description = "表示順", example = "20")
        int displayOrder,

        @Schema(description = "メッセージ", example = "勘定科目構成を更新しました")
        String message,

        @Schema(description = "エラーメッセージ", example = "勘定科目構成が見つかりません")
        String errorMessage
) {
    public static UpdateAccountStructureResponse success(
            String accountCode,
            String accountPath,
            int hierarchyLevel,
            String parentAccountCode,
            int displayOrder,
            String message
    ) {
        return new UpdateAccountStructureResponse(
                true,
                accountCode,
                accountPath,
                hierarchyLevel,
                parentAccountCode,
                displayOrder,
                message,
                null
        );
    }

    public static UpdateAccountStructureResponse failure(String errorMessage) {
        return new UpdateAccountStructureResponse(false, null, null, 0, null, 0, null, errorMessage);
    }
}
