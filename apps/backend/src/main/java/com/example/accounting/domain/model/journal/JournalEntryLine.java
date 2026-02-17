package com.example.accounting.domain.model.journal;

import com.example.accounting.domain.model.account.AccountId;
import io.vavr.control.Either;

/**
 * 仕訳明細行を表す値オブジェクト
 */
public record JournalEntryLine(
        Integer lineNumber,
        AccountId accountId,
        Money debitAmount,
        Money creditAmount
) {

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
     * バリデーション付きファクトリメソッド
     *
     * @param lineNumber  行番号
     * @param accountId   勘定科目 ID
     * @param debitAmount 借方金額
     * @param creditAmount 貸方金額
     * @return Either（左: エラーメッセージ、右: JournalEntryLine インスタンス）
     */
    public static Either<String, JournalEntryLine> validated(Integer lineNumber,
                                                              AccountId accountId,
                                                              Money debitAmount,
                                                              Money creditAmount) {
        if (lineNumber == null) {
            return Either.left("行番号は必須です");
        }
        if (accountId == null) {
            return Either.left("勘定科目 ID は必須です");
        }
        if ((debitAmount == null && creditAmount == null)
                || (debitAmount != null && creditAmount != null)) {
            return Either.left("借方または貸方のいずれか一方のみ金額を設定してください");
        }
        return Either.right(new JournalEntryLine(lineNumber, accountId, debitAmount, creditAmount));
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
