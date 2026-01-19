package com.example.accounting.domain.model.account;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.util.Objects;

/**
 * 勘定科目エンティティ
 */
@Value
@With
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {

    AccountId id;
    AccountCode accountCode;
    String accountName;
    AccountType accountType;

    /**
     * 新規作成用ファクトリメソッド
     *
     * <p>ID は null で作成され、DB 保存時に自動採番される。</p>
     *
     * @param code 勘定科目コード
     * @param name 勘定科目名
     * @param type 勘定科目種別
     * @return 新しい Account インスタンス
     */
    public static Account create(AccountCode code, String name, AccountType type) {
        Objects.requireNonNull(code, "勘定科目コードは必須です");
        Objects.requireNonNull(name, "勘定科目名は必須です");
        Objects.requireNonNull(type, "勘定科目種別は必須です");

        // ID は null（DB 保存時に自動採番される）
        return new Account(null, code, name, type);
    }

    /**
     * DB 復元用ファクトリメソッド
     *
     * @param id   勘定科目ID
     * @param code 勘定科目コード
     * @param name 勘定科目名
     * @param type 勘定科目種別
     * @return 復元された Account インスタンス
     */
    public static Account reconstruct(AccountId id,
                                      AccountCode code,
                                      String name,
                                      AccountType type) {
        return new Account(id, code, name, type);
    }
}
