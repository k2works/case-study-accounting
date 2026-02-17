package com.example.accounting.application.port.in.query;

import io.vavr.control.Either;

import java.time.LocalDate;
import java.util.List;

/**
 * 仕訳一覧取得クエリ
 *
 * @param page     ページ番号（0 始まり）
 * @param size     ページサイズ
 * @param statuses フィルタ対象ステータス（空の場合は全ステータス）
 * @param dateFrom 仕訳日付開始
 * @param dateTo   仕訳日付終了
 */
public record GetJournalEntriesQuery(
        int page,
        int size,
        List<String> statuses,
        LocalDate dateFrom,
        LocalDate dateTo
) {
    /**
     * コンパクトコンストラクタ - 防御的コピー
     */
    public GetJournalEntriesQuery {
        statuses = statuses == null ? List.of() : List.copyOf(statuses);
    }

    public static Either<String, GetJournalEntriesQuery> of(
            int page,
            int size,
            List<String> statuses,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        if (page < 0) {
            return Either.left("ページ番号は 0 以上である必要があります");
        }
        if (size < 1 || size > 100) {
            return Either.left("ページサイズは 1 以上 100 以下である必要があります");
        }
        return Either.right(new GetJournalEntriesQuery(page, size, statuses, dateFrom, dateTo));
    }

    public static GetJournalEntriesQuery defaultQuery() {
        return new GetJournalEntriesQuery(0, 20, List.of(), null, null);
    }

    public static GetJournalEntriesQuery ofPage(int page, int size) {
        return new GetJournalEntriesQuery(page, size, List.of(), null, null);
    }
}
