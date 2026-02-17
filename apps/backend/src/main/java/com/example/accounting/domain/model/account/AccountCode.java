package com.example.accounting.domain.model.account;

import io.vavr.control.Either;

import java.util.regex.Pattern;

/**
 * 勘定科目コードを表す値オブジェクト
 */
public record AccountCode(String value) {

    private static final Pattern ACCOUNT_CODE_PATTERN = Pattern.compile("^\\d{4}$");

    /**
     * 勘定科目コードを生成する
     *
     * @param value 勘定科目コード
     * @return AccountCode インスタンス
     */
    public static AccountCode of(String value) {
        return new AccountCode(value);
    }

    /**
     * バリデーション付きファクトリメソッド
     *
     * @param value 勘定科目コード
     * @return Either（左: エラーメッセージ、右: AccountCode インスタンス）
     */
    public static Either<String, AccountCode> validated(String value) {
        if (value == null || value.isBlank()) {
            return Either.left("勘定科目コードは必須です");
        }
        if (!ACCOUNT_CODE_PATTERN.matcher(value).matches()) {
            return Either.left("勘定科目コードは 4 桁の数字である必要があります");
        }
        return Either.right(new AccountCode(value));
    }

    /**
     * DB からの復元用ファクトリメソッド（バリデーションをスキップ）
     *
     * @param value 勘定科目コード
     * @return AccountCode インスタンス
     */
    public static AccountCode reconstruct(String value) {
        return new AccountCode(value);
    }

    public boolean isAssetAccount() {
        int prefix = categoryPrefix();
        return prefix >= 11 && prefix <= 19;
    }

    public boolean isLiabilityAccount() {
        int prefix = categoryPrefix();
        return prefix >= 21 && prefix <= 29;
    }

    public boolean isEquityAccount() {
        int prefix = categoryPrefix();
        return prefix >= 31 && prefix <= 39;
    }

    public boolean isRevenueAccount() {
        int prefix = categoryPrefix();
        return prefix >= 41 && prefix <= 49;
    }

    public boolean isExpenseAccount() {
        int prefix = categoryPrefix();
        return prefix >= 51 && prefix <= 79;
    }

    public boolean isBalanceSheetAccount() {
        return isAssetAccount() || isLiabilityAccount() || isEquityAccount();
    }

    public boolean isProfitLossAccount() {
        return isRevenueAccount() || isExpenseAccount();
    }

    private int categoryPrefix() {
        return Integer.parseInt(value.substring(0, 2));
    }

    @Override
    public String toString() {
        return value;
    }
}
