package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "自動仕訳パターン登録リクエスト")
public record CreateAutoJournalPatternRequest(
        @Schema(description = "パターンコード", example = "AP001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String patternCode,

        @Schema(description = "パターン名", example = "売上計上", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String patternName,

        @Schema(description = "ソーステーブル名", example = "sales", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String sourceTableName,

        @Schema(description = "説明", example = "売上データから自動仕訳を生成")
        String description,

        @Schema(description = "パターン明細", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Size(min = 1)
        @Valid
        List<PatternItemRequest> items
) {
    public CreateAutoJournalPatternRequest {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public record PatternItemRequest(
            @Schema(description = "行番号", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull
            Integer lineNumber,

            @Schema(description = "貸借区分（D/C）", example = "D", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank
            String debitCreditType,

            @Schema(description = "勘定科目コード", example = "1100", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank
            String accountCode,

            @Schema(description = "金額計算式", example = "amount", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank
            String amountFormula,

            @Schema(description = "摘要テンプレート", example = "売上 {id}")
            String descriptionTemplate
    ) {
    }
}
