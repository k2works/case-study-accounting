package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 仕訳編集レスポンス
 *
 * @param success        成功フラグ
 * @param journalEntryId 仕訳ID
 * @param journalDate    仕訳日
 * @param description    摘要
 * @param status         ステータス
 * @param version        バージョン
 * @param message        成功メッセージ
 * @param errorMessage   エラーメッセージ
 */
@Schema(description = "仕訳編集レスポンス")
public record UpdateJournalEntryResponse(
        @Schema(description = "更新成功フラグ", example = "true")
        boolean success,

        @Schema(description = "仕訳ID（更新成功時のみ）", example = "1")
        Integer journalEntryId,

        @Schema(description = "仕訳日（更新成功時のみ）", example = "2024-01-31")
        LocalDate journalDate,

        @Schema(description = "摘要（更新成功時のみ）", example = "売上計上")
        String description,

        @Schema(description = "ステータス（更新成功時のみ）", example = "DRAFT")
        String status,

        @Schema(description = "バージョン（更新成功時のみ）", example = "2")
        Integer version,

        @Schema(description = "成功メッセージ（更新成功時のみ）", example = "仕訳を更新しました")
        String message,

        @Schema(description = "エラーメッセージ（更新失敗時のみ）", example = "仕訳が見つかりません")
        String errorMessage
) {
    /**
     * 成功レスポンスを生成
     */
    public static UpdateJournalEntryResponse success(
            Integer journalEntryId,
            LocalDate journalDate,
            String description,
            String status,
            Integer version,
            String message
    ) {
        return new UpdateJournalEntryResponse(
                true,
                journalEntryId,
                journalDate,
                description,
                status,
                version,
                message,
                null
        );
    }

    /**
     * 失敗レスポンスを生成
     */
    public static UpdateJournalEntryResponse failure(String errorMessage) {
        return new UpdateJournalEntryResponse(false, null, null, null, null, null, null, errorMessage);
    }
}
