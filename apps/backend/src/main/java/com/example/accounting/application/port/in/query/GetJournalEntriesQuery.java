package com.example.accounting.application.port.in.query;

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
     * コンパクトコンストラクタ - バリデーションと防御的コピー
     */
    public GetJournalEntriesQuery {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        statuses = statuses == null ? List.of() : List.copyOf(statuses);
    }

    public static GetJournalEntriesQuery defaultQuery() {
        return new GetJournalEntriesQuery(0, 20, List.of(), null, null);
    }

    public static GetJournalEntriesQuery ofPage(int page, int size) {
        return new GetJournalEntriesQuery(page, size, List.of(), null, null);
    }
}
