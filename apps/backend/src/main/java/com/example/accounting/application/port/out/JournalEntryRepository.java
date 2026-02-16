package com.example.accounting.application.port.out;

import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;
import com.example.accounting.application.port.out.GetGeneralLedgerResult.GeneralLedgerEntry;
import com.example.accounting.application.port.out.GetDailyBalanceResult.DailyBalanceEntry;
import io.vavr.control.Try;

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
     * @return Try でラップされた保存済み仕訳
     */
    Try<JournalEntry> save(JournalEntry journalEntry);

    /**
     * 仕訳IDで仕訳を検索する
     *
     * @param id 仕訳ID
     * @return Try でラップされた仕訳（存在しない場合は empty）
     */
    Try<Optional<JournalEntry>> findById(JournalEntryId id);

    /**
     * すべての仕訳を取得する
     *
     * @return Try でラップされた仕訳リスト
     */
    Try<List<JournalEntry>> findAll();

    /**
     * 条件付きで仕訳一覧を取得する（ページネーション対応）
     *
     * @param statuses フィルタ対象ステータス（空の場合は全ステータス）
     * @param dateFrom 仕訳日付開始（null 可）
     * @param dateTo 仕訳日付終了（null 可）
     * @param offset オフセット
     * @param limit 取得件数
     * @return Try でラップされた仕訳リスト
     */
    Try<List<JournalEntry>> findByConditions(
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
     * @return Try でラップされた件数
     */
    Try<Long> countByConditions(List<String> statuses, LocalDate dateFrom, LocalDate dateTo);

    /**
     * 検索条件で仕訳一覧を取得する（ページネーション対応）
     *
     * @param criteria 検索条件
     * @return Try でラップされた仕訳リスト
     */
    Try<List<JournalEntry>> searchByConditions(JournalEntrySearchCriteria criteria);

    /**
     * 検索条件に一致する仕訳件数を取得する
     *
     * @param criteria 検索条件
     * @return Try でラップされた件数
     */
    Try<Long> countBySearchConditions(JournalEntrySearchCriteria criteria);

    /**
     * 総勘定元帳用に確定仕訳行を取得する
     *
     * @param accountId 勘定科目 ID
     * @param dateFrom 仕訳日付開始（null 可）
     * @param dateTo 仕訳日付終了（null 可）
     * @param offset オフセット
     * @param limit 取得件数
     * @return Try でラップされた仕訳行リスト
     */
    Try<List<GeneralLedgerEntry>> findPostedLinesByAccountAndPeriod(
            Integer accountId,
            LocalDate dateFrom,
            LocalDate dateTo,
            int offset,
            int limit
    );

    /**
     * 総勘定元帳用に確定仕訳行件数を取得する
     *
     * @param accountId 勘定科目 ID
     * @param dateFrom 仕訳日付開始（null 可）
     * @param dateTo 仕訳日付終了（null 可）
     * @return Try でラップされた件数
     */
    Try<Long> countPostedLinesByAccountAndPeriod(Integer accountId, LocalDate dateFrom, LocalDate dateTo);

    /**
     * 日次残高集計を取得する
     *
     * @param accountId 勘定科目 ID
     * @param dateFrom 仕訳日付開始（null 可）
     * @param dateTo 仕訳日付終了（null 可）
     * @return Try でラップされた日次残高一覧
     */
    Try<List<DailyBalanceEntry>> findDailyBalanceByAccountAndPeriod(
            Integer accountId,
            LocalDate dateFrom,
            LocalDate dateTo
    );

    /**
     * 指定日より前の残高を計算する
     *
     * @param accountId 勘定科目 ID
     * @param date 指定日
     * @return Try でラップされた残高
     */
    Try<BigDecimal> calculateBalanceBeforeDate(Integer accountId, LocalDate date);

    /**
     * 仕訳を削除する
     *
     * @param id 仕訳ID
     * @return Try でラップされた削除結果
     */
    Try<Void> deleteById(JournalEntryId id);
}
