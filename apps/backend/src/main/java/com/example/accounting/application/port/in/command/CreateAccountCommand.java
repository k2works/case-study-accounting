package com.example.accounting.application.port.in.command;

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
            "ASSET",
            "LIABILITY",
            "EQUITY",
            "REVENUE",
            "EXPENSE"
    );

    public CreateAccountCommand {
        if (accountCode == null || accountCode.isBlank()) {
            throw new IllegalArgumentException("勘定科目コードは必須です");
        }
        if (!ACCOUNT_CODE_PATTERN.matcher(accountCode).matches()) {
            throw new IllegalArgumentException("勘定科目コードは 4 桁の数字である必要があります");
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
