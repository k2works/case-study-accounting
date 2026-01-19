package com.example.accounting.domain.model.account;

import java.util.Arrays;

/**
 * 勘定科目種別を表す列挙型
 */
public enum AccountType {
    ASSET("資産", "B", true),
    LIABILITY("負債", "B", false),
    EQUITY("純資産", "B", false),
    REVENUE("収益", "P", false),
    EXPENSE("費用", "P", true);

    private final String displayName;
    private final String bsPlType;
    private final boolean debitBalance;

    AccountType(String displayName, String bsPlType, boolean debitBalance) {
        this.displayName = displayName;
        this.bsPlType = bsPlType;
        this.debitBalance = debitBalance;
    }

    /**
     * コードから勘定科目種別を取得する
     *
     * @param code 種別コード
     * @return 対応する勘定科目種別
     * @throws IllegalArgumentException 無効なコードの場合
     */
    public static AccountType fromCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.name().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "無効な勘定科目種別コードです: " + code));
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBsPlType() {
        return bsPlType;
    }

    public boolean isDebitBalance() {
        return debitBalance;
    }

    public boolean isBalanceSheet() {
        return "B".equals(bsPlType);
    }

    public boolean isProfitAndLoss() {
        return "P".equals(bsPlType);
    }
}
