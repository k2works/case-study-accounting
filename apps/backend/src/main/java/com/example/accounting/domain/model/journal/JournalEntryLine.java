package com.example.accounting.domain.model.journal;

import com.example.accounting.domain.model.account.AccountId;

/**
 * 仕訳明細行を表す値オブジェクト
 */
public record JournalEntryLine(
        Integer lineNumber,
        AccountId accountId,
        Money debitAmount,
        Money creditAmount
) {

    public JournalEntryLine {
        if (lineNumber == null) {
            throw new IllegalArgumentException("行番号は必須です");
        }
        if (accountId == null) {
            throw new IllegalArgumentException("勘定科目 ID は必須です");
        }
        if ((debitAmount == null && creditAmount == null)
                || (debitAmount != null && creditAmount != null)) {
            throw new IllegalArgumentException("借方または貸方のいずれか一方のみ金額を設定してください");
        }
    }

    /**
     * 明細行を生成する
     *
     * @param lineNumber  行番号
     * @param accountId   勘定科目 ID
     * @param debitAmount 借方金額
     * @param creditAmount 貸方金額
     * @return JournalEntryLine インスタンス
     */
    public static JournalEntryLine of(Integer lineNumber,
                                      AccountId accountId,
                                      Money debitAmount,
                                      Money creditAmount) {
        return new JournalEntryLine(lineNumber, accountId, debitAmount, creditAmount);
    }

    /**
     * DB からの復元用ファクトリメソッド
     *
     * @param lineNumber  行番号
     * @param accountId   勘定科目 ID
     * @param debitAmount 借方金額
     * @param creditAmount 貸方金額
     * @return JournalEntryLine インスタンス
     */
    public static JournalEntryLine reconstruct(Integer lineNumber,
                                               AccountId accountId,
                                               Money debitAmount,
                                               Money creditAmount) {
        return new JournalEntryLine(lineNumber, accountId, debitAmount, creditAmount);
    }

    public boolean isDebit() {
        return debitAmount != null;
    }

    public boolean isCredit() {
        return creditAmount != null;
    }
}
