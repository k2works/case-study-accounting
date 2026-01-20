package com.example.accounting.infrastructure.persistence.checker;

import com.example.accounting.application.port.out.AccountUsageChecker;
import com.example.accounting.domain.model.account.AccountId;
import org.springframework.stereotype.Repository;

/**
 * 勘定科目使用状況確認（スタブ実装）
 */
@Repository
public class AccountUsageCheckerImpl implements AccountUsageChecker {

    @Override
    public boolean isAccountInUse(AccountId accountId) {
        return false;
    }
}
