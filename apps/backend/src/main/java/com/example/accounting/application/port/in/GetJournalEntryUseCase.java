package com.example.accounting.application.port.in;

import com.example.accounting.application.port.out.JournalEntryDetailResult;
import java.util.List;
import java.util.Optional;

/**
 * 仕訳取得ユースケース
 */
public interface GetJournalEntryUseCase {
    /**
     * 仕訳IDで仕訳を取得する
     */
    Optional<JournalEntryDetailResult> findById(Integer id);

    /**
     * 全ての仕訳を取得する
     */
    List<JournalEntryDetailResult> findAll();
}
