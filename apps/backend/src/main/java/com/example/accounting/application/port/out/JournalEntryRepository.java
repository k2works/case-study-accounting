package com.example.accounting.application.port.out;

import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;

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
     * 仕訳を削除する
     *
     * @param id 仕訳ID
     */
    void deleteById(JournalEntryId id);
}
