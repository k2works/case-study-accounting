package com.example.accounting.application.port.in.command;

import io.vavr.control.Either;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 勘定科目登録コマンド
 *
 * @param accountCode 勘定科目コード
 * @param accountName 勘定科目名
 * @param accountType 勘定科目種別
 */
public record CreateAccountCommand(
        String accountCode,
        String accountName,
        String accountType
) {
    private static final Pattern ACCOUNT_CODE_PATTERN = Pattern.compile("^\\d{4}$");
    private static final Set<String> ALLOWED_ACCOUNT_TYPES = Set.of(
            "ASSET", "LIABILITY", "EQUITY", "REVENUE", "EXPENSE"
    );

    public static Either<String, CreateAccountCommand> of(
            String accountCode,
            String accountName,
            String accountType
    ) {
        return validateAccountCode(accountCode)
                .flatMap(v -> validateAccountName(accountName))
                .flatMap(v -> validateAccountType(accountType))
                .map(v -> new CreateAccountCommand(accountCode, accountName, accountType));
    }

    private static Either<String, Void> validateAccountCode(String accountCode) {
        if (accountCode == null || accountCode.isBlank()) {
            return Either.left("勘定科目コードは必須です");
        }
        if (!ACCOUNT_CODE_PATTERN.matcher(accountCode).matches()) {
            return Either.left("勘定科目コードは 4 桁の数字である必要があります");
        }
        return Either.right(null);
    }

    private static Either<String, Void> validateAccountName(String accountName) {
        if (accountName == null || accountName.isBlank()) {
            return Either.left("勘定科目名は必須です");
        }
        return Either.right(null);
    }

    private static Either<String, Void> validateAccountType(String accountType) {
        if (accountType == null || accountType.isBlank()) {
            return Either.left("勘定科目種別は必須です");
        }
        if (!ALLOWED_ACCOUNT_TYPES.contains(accountType)) {
            return Either.left("無効な勘定科目種別コードです: " + accountType);
        }
        return Either.right(null);
    }
}
