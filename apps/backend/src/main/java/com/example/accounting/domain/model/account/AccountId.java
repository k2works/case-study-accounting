package com.example.accounting.domain.model.account;

import java.util.Objects;
import java.util.UUID;

/**
 * 勘定科目IDを表す値オブジェクト
 *
 * <p>UUID を利用して一意な Integer 値を生成する。</p>
 */
public record AccountId(Integer value) {

    public AccountId {
        Objects.requireNonNull(value, "勘定科目IDは必須です");
    }

    /**
     * 新しい勘定科目IDを生成する
     *
     * @return 新しい勘定科目ID
     */
    public static AccountId generate() {
        return new AccountId(UUID.randomUUID().hashCode());
    }

    /**
     * Integer から勘定科目IDを生成する
     *
     * @param value 勘定科目ID
     * @return 勘定科目ID
     */
    public static AccountId of(Integer value) {
        return new AccountId(value);
    }
}
