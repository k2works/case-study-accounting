package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "勘定科目構成登録レスポンス")
public record CreateAccountStructureResponse(
        @Schema(description = "登録成功フラグ", example = "true")
        boolean success,

        @Schema(description = "勘定科目コード", example = "1100")
        String accountCode,

        @Schema(description = "勘定科目パス", example = "1000~1100")
        String accountPath,

        @Schema(description = "階層レベル", example = "2")
        int hierarchyLevel,

        @Schema(description = "親勘定科目コード", example = "1000")
        String parentAccountCode,

        @Schema(description = "表示順", example = "0")
        int displayOrder,

        @Schema(description = "エラーメッセージ", example = "勘定科目構成は既に登録されています")
        String errorMessage
) {
    public static CreateAccountStructureResponse success(
            String accountCode,
            String accountPath,
            int hierarchyLevel,
            String parentAccountCode,
            int displayOrder
    ) {
        return new CreateAccountStructureResponse(
                true,
                accountCode,
                accountPath,
                hierarchyLevel,
                parentAccountCode,
                displayOrder,
                null
        );
    }

    public static CreateAccountStructureResponse failure(String errorMessage) {
        return new CreateAccountStructureResponse(false, null, null, 0, null, 0, errorMessage);
    }
}
