package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "自動仕訳パターン更新レスポンス")
public record UpdateAutoJournalPatternResponse(
        @Schema(description = "更新成功フラグ", example = "true")
        boolean success,

        @Schema(description = "パターン ID（更新成功時のみ）", example = "1")
        Long patternId,

        @Schema(description = "パターンコード（更新成功時のみ）", example = "AP001")
        String patternCode,

        @Schema(description = "パターン名（更新成功時のみ）", example = "売上計上")
        String patternName,

        @Schema(description = "確認メッセージ（更新成功時のみ）", example = "自動仕訳パターンを更新しました")
        String message,

        @Schema(description = "エラーメッセージ（更新失敗時のみ）", example = "自動仕訳パターンが見つかりません")
        String errorMessage
) {
    public static UpdateAutoJournalPatternResponse success(
            Long patternId,
            String patternCode,
            String patternName,
            String message
    ) {
        return new UpdateAutoJournalPatternResponse(true, patternId, patternCode, patternName, message, null);
    }

    public static UpdateAutoJournalPatternResponse failure(String errorMessage) {
        return new UpdateAutoJournalPatternResponse(false, null, null, null, null, errorMessage);
    }
}
