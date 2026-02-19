package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "自動仕訳パターン登録レスポンス")
public record CreateAutoJournalPatternResponse(
        @Schema(description = "登録成功フラグ", example = "true")
        boolean success,

        @Schema(description = "パターン ID（登録成功時のみ）", example = "1")
        Long patternId,

        @Schema(description = "パターンコード（登録成功時のみ）", example = "AP001")
        String patternCode,

        @Schema(description = "パターン名（登録成功時のみ）", example = "売上計上")
        String patternName,

        @Schema(description = "エラーメッセージ（登録失敗時のみ）", example = "パターンコードは既に使用されています")
        String errorMessage
) {
    public static CreateAutoJournalPatternResponse success(
            Long patternId,
            String patternCode,
            String patternName
    ) {
        return new CreateAutoJournalPatternResponse(true, patternId, patternCode, patternName, null);
    }

    public static CreateAutoJournalPatternResponse failure(String errorMessage) {
        return new CreateAutoJournalPatternResponse(false, null, null, null, errorMessage);
    }
}
