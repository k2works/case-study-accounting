package com.example.accounting.application.port.out;

import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
import io.vavr.control.Try;

import java.util.List;
import java.util.Optional;

/**
 * 勘定科目リポジトリインターフェース（Output Port）
 *
 * <p>アプリケーション層で定義される Output Port。
 * 実装はインフラストラクチャ層（infrastructure.persistence.repository）で行う。</p>
 */
public interface AccountRepository {

    /**
     * 勘定科目を保存する
     *
     * @param account 勘定科目
     * @return 保存された勘定科目を含む Try
     */
    Try<Account> save(Account account);

    /**
     * 勘定科目IDで勘定科目を検索する
     *
     * @param id 勘定科目ID
     * @return 勘定科目（存在しない場合は empty）を含む Try
     */
    Try<Optional<Account>> findById(AccountId id);

    /**
     * 勘定科目コードで勘定科目を検索する
     *
     * @param code 勘定科目コード
     * @return 勘定科目（存在しない場合は empty）を含む Try
     */
    Try<Optional<Account>> findByCode(AccountCode code);

    /**
     * 勘定科目コードで勘定科目を検索する
     *
     * @param code 勘定科目コード
     * @return 勘定科目（存在しない場合は empty）を含む Try
     */
    Try<Optional<Account>> findByCode(String code);

    /**
     * すべての勘定科目を取得する
     *
     * @return 勘定科目リストを含む Try
     */
    Try<List<Account>> findAll();

    /**
     * 勘定科目種別で勘定科目を検索する
     *
     * @param type 勘定科目種別
     * @return 勘定科目リストを含む Try
     */
    Try<List<Account>> findByType(AccountType type);

    /**
     * 検索条件に基づいて勘定科目を検索する
     *
     * @param type 勘定科目種別（null の場合は全種別）
     * @param keyword 検索キーワード（null の場合は全件）
     * @return 勘定科目リストを含む Try
     */
    Try<List<Account>> search(AccountType type, String keyword);

    /**
     * 勘定科目を削除する
     *
     * @param id 勘定科目ID
     * @return 処理結果を含む Try
     */
    Try<Void> deleteById(AccountId id);

    /**
     * 勘定科目コードが存在するかチェックする
     *
     * @param code 勘定科目コード
     * @return 存在する場合 true を含む Try
     */
    Try<Boolean> existsByCode(AccountCode code);
}
