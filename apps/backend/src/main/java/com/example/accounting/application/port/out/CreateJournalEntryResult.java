package com.example.accounting.application.port.out;

import java.time.LocalDate;

/**
 * 仕訳登録結果
 *
 * @param success         登録成功かどうか
 * @param journalEntryId  仕訳ID（成功時のみ）
 * @param journalDate     仕訳日（成功時のみ）
 * @param description     摘要（成功時のみ）
 * @param status          ステータス（成功時のみ）
 * @param errorMessage    エラーメッセージ（失敗時のみ）
 */
public record CreateJournalEntryResult(
        boolean success,
        Integer journalEntryId,
        LocalDate journalDate,
        String description,
        String status,
        String errorMessage
) {
    /**
     * 登録成功結果を生成する
     */
    public static CreateJournalEntryResult success(
            Integer journalEntryId,
            LocalDate journalDate,
            String description,
            String status
    ) {
        return new CreateJournalEntryResult(true, journalEntryId, journalDate, description, status, null);
    }

    /**
     * 登録失敗結果を生成する
     */
    public static CreateJournalEntryResult failure(String errorMessage) {
        return new CreateJournalEntryResult(false, null, null, null, null, errorMessage);
    }
}
