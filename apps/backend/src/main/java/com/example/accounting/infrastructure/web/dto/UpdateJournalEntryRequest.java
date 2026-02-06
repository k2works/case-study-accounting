package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 仕訳編集リクエスト
 */
@Schema(description = "仕訳編集リクエスト")
public record UpdateJournalEntryRequest(
        @Schema(description = "仕訳日", example = "2024-01-31")
        @NotNull(message = "仕訳日を入力してください")
        LocalDate journalDate,

        @Schema(description = "摘要", example = "売上計上")
        @NotBlank(message = "摘要を入力してください")
        String description,

        @Schema(description = "仕訳明細")
        @NotEmpty(message = "明細を入力してください")
        @Valid
        List<JournalEntryLineRequest> lines,

        @Schema(description = "バージョン", example = "1")
        @NotNull(message = "バージョンを入力してください")
        Integer version
) {
    /**
     * コンパクトコンストラクタ - 防御的コピーを作成
     */
    public UpdateJournalEntryRequest {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    @Schema(description = "仕訳明細行リクエスト")
    public record JournalEntryLineRequest(
            @Schema(description = "行番号")
            @NotNull Integer lineNumber,

            @Schema(description = "勘定科目ID")
            @NotNull Integer accountId,

            @Schema(description = "借方金額")
            BigDecimal debitAmount,

            @Schema(description = "貸方金額")
            BigDecimal creditAmount
    ) {
    }
}
