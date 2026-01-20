package com.example.accounting.application.port.out;

import com.example.accounting.domain.model.account.AccountId;

/**
 * 勘定科目の使用状況確認（Output Port）
 */
public interface AccountUsageChecker {

    /**
     * 勘定科目が仕訳で使用されているか確認する
     *
     * @param accountId 勘定科目ID
     * @return 使用中の場合 true
     */
    boolean isAccountInUse(AccountId accountId);
}
