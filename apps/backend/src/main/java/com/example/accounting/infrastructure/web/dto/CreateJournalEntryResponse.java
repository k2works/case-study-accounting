package com.example.accounting.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 仕訳登録レスポンス
 *
 * @param success        成功フラグ
 * @param journalEntryId 仕訳ID
 * @param journalDate    仕訳日
 * @param description    摘要
 * @param status         ステータス
 * @param errorMessage   エラーメッセージ
 */
@Schema(description = "仕訳登録レスポンス")
public record CreateJournalEntryResponse(
        @Schema(description = "登録成功フラグ", example = "true")
        boolean success,

        @Schema(description = "仕訳ID（登録成功時のみ）", example = "1")
        Integer journalEntryId,

        @Schema(description = "仕訳日（登録成功時のみ）", example = "2024-01-31")
        LocalDate journalDate,

        @Schema(description = "摘要（登録成功時のみ）", example = "売上計上")
        String description,

        @Schema(description = "ステータス（登録成功時のみ）", example = "DRAFT")
        String status,

        @Schema(description = "エラーメッセージ（登録失敗時のみ）", example = "貸借一致していません")
        String errorMessage
) {
    /**
     * 成功レスポンスを生成
     */
    public static CreateJournalEntryResponse success(
            Integer journalEntryId,
            LocalDate journalDate,
            String description,
            String status
    ) {
        return new CreateJournalEntryResponse(true, journalEntryId, journalDate, description, status, null);
    }

    /**
     * 失敗レスポンスを生成
     */
    public static CreateJournalEntryResponse failure(String errorMessage) {
        return new CreateJournalEntryResponse(false, null, null, null, null, errorMessage);
    }
}
