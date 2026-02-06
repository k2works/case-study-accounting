package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.query.SearchJournalEntriesQuery;
import com.example.accounting.application.port.out.GetJournalEntriesResult;

/**
 * 仕訳検索ユースケース
 */
public interface SearchJournalEntriesUseCase {
    GetJournalEntriesResult execute(SearchJournalEntriesQuery query);
}
