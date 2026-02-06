package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.query.GetJournalEntriesQuery;
import com.example.accounting.application.port.out.GetJournalEntriesResult;

/**
 * 仕訳一覧取得ユースケース
 */
public interface GetJournalEntriesUseCase {
    /**
     * 仕訳一覧をページネーション付きで取得する
     */
    GetJournalEntriesResult execute(GetJournalEntriesQuery query);
}
