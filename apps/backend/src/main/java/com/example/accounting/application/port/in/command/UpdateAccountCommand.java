package com.example.accounting.application.port.in.command;

import io.vavr.control.Either;

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

    public static Either<String, UpdateAccountCommand> of(
            Integer accountId,
            String accountName,
            String accountType
    ) {
        if (accountId == null) {
            return Either.left("勘定科目IDは必須です");
        }
        if (accountName == null || accountName.isBlank()) {
            return Either.left("勘定科目名は必須です");
        }
        if (accountType == null || accountType.isBlank()) {
            return Either.left("勘定科目種別は必須です");
        }
        if (!ALLOWED_ACCOUNT_TYPES.contains(accountType)) {
            return Either.left("無効な勘定科目種別コードです: " + accountType);
        }
        return Either.right(new UpdateAccountCommand(accountId, accountName, accountType));
    }
}
