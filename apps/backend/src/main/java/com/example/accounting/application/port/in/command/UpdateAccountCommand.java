package com.example.accounting.application.port.in.command;

import java.util.Set;

/**
 * 勘定科目更新コマンド
 *
 * @param accountId   勘定科目ID
 * @param accountName 勘定科目名
 * @param accountType 勘定科目種別
 */
public record UpdateAccountCommand(
        Integer accountId,
        String accountName,
        String accountType
) {
    private static final Set<String> ALLOWED_ACCOUNT_TYPES = Set.of(
            "ASSET",
            "LIABILITY",
            "EQUITY",
            "REVENUE",
            "EXPENSE"
    );

    public UpdateAccountCommand {
        if (accountId == null) {
            throw new IllegalArgumentException("勘定科目IDは必須です");
        }
        if (accountName == null || accountName.isBlank()) {
            throw new IllegalArgumentException("勘定科目名は必須です");
        }
        if (accountType == null || accountType.isBlank()) {
            throw new IllegalArgumentException("勘定科目種別は必須です");
        }
        if (!ALLOWED_ACCOUNT_TYPES.contains(accountType)) {
            throw new IllegalArgumentException("無効な勘定科目種別コードです: " + accountType);
        }
    }
}
