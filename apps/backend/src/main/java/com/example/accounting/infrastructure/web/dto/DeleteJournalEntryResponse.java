package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 仕訳削除レスポンス
 *
 * @param success      成功フラグ
 * @param message      成功メッセージ
 * @param errorMessage エラーメッセージ
 */
@Schema(description = "仕訳削除レスポンス")
public record DeleteJournalEntryResponse(
        @Schema(description = "削除成功フラグ", example = "true")
        boolean success,

        @Schema(description = "成功メッセージ（削除成功時のみ）", example = "仕訳を削除しました")
        String message,

        @Schema(description = "エラーメッセージ（削除失敗時のみ）", example = "仕訳が見つかりません")
        String errorMessage
) {
    /**
     * 成功レスポンスを生成
     */
    public static DeleteJournalEntryResponse success(String message) {
        return new DeleteJournalEntryResponse(true, message, null);
    }

    /**
     * 失敗レスポンスを生成
     */
    public static DeleteJournalEntryResponse failure(String errorMessage) {
        return new DeleteJournalEntryResponse(false, null, errorMessage);
    }
}
