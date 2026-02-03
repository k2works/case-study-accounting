package com.example.accounting.application.port.out;

import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 仕訳リポジトリインターフェース（Output Port）
 *
 * <p>アプリケーション層で定義される Output Port。
 * 実装はインフラストラクチャ層（infrastructure.persistence.repository）で行う。</p>
 */
public interface JournalEntryRepository {

    /**
     * 仕訳を保存する
     *
     * @param journalEntry 仕訳
     * @return 保存された仕訳
     */
    JournalEntry save(JournalEntry journalEntry);

    /**
     * 仕訳IDで仕訳を検索する
     *
     * @param id 仕訳ID
     * @return 仕訳（存在しない場合は empty）
     */
    Optional<JournalEntry> findById(JournalEntryId id);

    /**
     * すべての仕訳を取得する
     *
     * @return 仕訳リスト
     */
    List<JournalEntry> findAll();

    /**
     * 条件付きで仕訳一覧を取得する（ページネーション対応）
     *
     * @param statuses フィルタ対象ステータス（空の場合は全ステータス）
     * @param dateFrom 仕訳日付開始（null 可）
     * @param dateTo 仕訳日付終了（null 可）
     * @param offset オフセット
     * @param limit 取得件数
     * @return 仕訳リスト
     */
    List<JournalEntry> findByConditions(
            List<String> statuses,
            LocalDate dateFrom,
            LocalDate dateTo,
            int offset,
            int limit
    );

    /**
     * 条件に一致する仕訳件数を取得する
     *
     * @param statuses フィルタ対象ステータス（空の場合は全ステータス）
     * @param dateFrom 仕訳日付開始（null 可）
     * @param dateTo 仕訳日付終了（null 可）
     * @return 件数
     */
    long countByConditions(List<String> statuses, LocalDate dateFrom, LocalDate dateTo);

    /**
     * 検索条件で仕訳一覧を取得する（ページネーション対応）
     */
    List<JournalEntry> searchByConditions(
            List<String> statuses,
            LocalDate dateFrom,
            LocalDate dateTo,
            Integer accountId,
            BigDecimal amountFrom,
            BigDecimal amountTo,
            String description,
            int offset,
            int limit
    );

    /**
     * 検索条件に一致する仕訳件数を取得する
     */
    long countBySearchConditions(
            List<String> statuses,
            LocalDate dateFrom,
            LocalDate dateTo,
            Integer accountId,
            BigDecimal amountFrom,
            BigDecimal amountTo,
            String description
    );

    /**
     * 仕訳を削除する
     *
     * @param id 仕訳ID
     */
    void deleteById(JournalEntryId id);
}
