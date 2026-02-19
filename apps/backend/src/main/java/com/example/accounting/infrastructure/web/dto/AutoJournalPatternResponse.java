package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "自動仕訳パターンレスポンス")
public record AutoJournalPatternResponse(
        @Schema(description = "パターン ID", example = "1")
        Long patternId,

        @Schema(description = "パターンコード", example = "AP001")
        String patternCode,

        @Schema(description = "パターン名", example = "売上計上")
        String patternName,

        @Schema(description = "ソーステーブル名", example = "sales")
        String sourceTableName,

        @Schema(description = "説明", example = "売上データから自動仕訳を生成")
        String description,

        @Schema(description = "有効フラグ", example = "true")
        Boolean isActive,

        @Schema(description = "パターン明細")
        List<ItemResponse> items
) {
    public AutoJournalPatternResponse {
        items = items == null ? List.of() : List.copyOf(items);
    }

    @Schema(description = "自動仕訳パターン明細レスポンス")
    public record ItemResponse(
            @Schema(description = "行番号", example = "1")
            Integer lineNumber,

            @Schema(description = "貸借区分（D/C）", example = "D")
            String debitCreditType,

            @Schema(description = "勘定科目コード", example = "1100")
            String accountCode,

            @Schema(description = "金額計算式", example = "amount")
            String amountFormula,

            @Schema(description = "摘要テンプレート", example = "売上 {id}")
            String descriptionTemplate
    ) {
    }
}
