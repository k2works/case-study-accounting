package com.example.accounting.domain.model.journal;

import com.example.accounting.domain.model.user.UserId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 仕訳エンティティ
 */
@Value
@With
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JournalEntry {

    JournalEntryId id;
    LocalDate journalDate;
    String description;
    JournalEntryStatus status;
    UserId approvedBy;
    LocalDateTime approvedAt;
    Integer version;
    List<JournalEntryLine> lines;
    UserId createdBy;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    /**
     * 新規作成用ファクトリメソッド
     *
     * @param journalDate 仕訳日
     * @param description 摘要
     * @param createdBy   作成者
     * @return 新しい JournalEntry インスタンス
     */
    public static JournalEntry create(LocalDate journalDate,
                                      String description,
                                      UserId createdBy,
                                      Integer version) {
        return create(journalDate, description, createdBy, version, LocalDateTime::now);
    }

    /**
     * 新規作成用ファクトリメソッド（時刻注入可能）
     *
     * @param journalDate 仕訳日
     * @param description 摘要
     * @param createdBy   作成者
     * @param clockSupplier 現在時刻取得関数
     * @return 新しい JournalEntry インスタンス
     */
    public static JournalEntry create(LocalDate journalDate,
                                      String description,
                                      UserId createdBy,
                                      Integer version,
                                      Supplier<LocalDateTime> clockSupplier) {
        if (journalDate == null) {
            throw new IllegalArgumentException("仕訳日は必須です");
        }
        if (description == null) {
            throw new IllegalArgumentException("摘要は必須です");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("作成者は必須です");
        }
        if (clockSupplier == null) {
            throw new IllegalArgumentException("時刻取得関数は必須です");
        }

        LocalDateTime now = clockSupplier.get();

        return new JournalEntry(
                null,
                journalDate,
                description,
                JournalEntryStatus.DRAFT,
                null,
                null,
                version,
                List.of(),
                createdBy,
                now,
                now
        );
    }

    /**
     * DB 復元用ファクトリメソッド
     *
     * @param id          仕訳 ID
     * @param journalDate 仕訳日
     * @param description 摘要
     * @param status      ステータス
     * @param lines       明細行
     * @param createdBy   作成者
     * @param createdAt   作成日時
     * @param updatedAt   更新日時
     * @return 復元された JournalEntry インスタンス
     */
    @SuppressWarnings("java:S107") // DB 復元用ファクトリメソッドのため全フィールドが必須
    public static JournalEntry reconstruct(JournalEntryId id,
                                           LocalDate journalDate,
                                           String description,
                                           JournalEntryStatus status,
                                           Integer version,
                                           List<JournalEntryLine> lines,
                                           UserId createdBy,
                                           UserId approvedBy,
                                           LocalDateTime approvedAt,
                                           LocalDateTime createdAt,
                                           LocalDateTime updatedAt) {
        return new JournalEntry(
                id,
                journalDate,
                description,
                status,
                approvedBy,
                approvedAt,
                version,
                List.copyOf(requireLines(lines)),
                createdBy,
                createdAt,
                updatedAt
        );
    }

    /**
     * 仕訳を更新した新しいインスタンスを返す
     *
     * @param newJournalDate 新しい仕訳日
     * @param newDescription 新しい摘要
     * @param newLines       新しい明細行
     * @return 更新された JournalEntry インスタンス
     * @throws IllegalStateException 下書き以外のステータスの場合
     */
    public JournalEntry update(LocalDate newJournalDate,
                               String newDescription,
                               List<JournalEntryLine> newLines) {
        return update(newJournalDate, newDescription, newLines, LocalDateTime::now);
    }

    public JournalEntry update(LocalDate newJournalDate,
                               String newDescription,
                               List<JournalEntryLine> newLines,
                               Supplier<LocalDateTime> clockSupplier) {
        if (status != JournalEntryStatus.DRAFT) {
            throw new IllegalStateException("下書き状態の仕訳のみ編集可能です");
        }
        if (newJournalDate == null) {
            throw new IllegalArgumentException("仕訳日は必須です");
        }
        if (newDescription == null) {
            throw new IllegalArgumentException("摘要は必須です");
        }

        return this.toBuilder()
                .journalDate(newJournalDate)
                .description(newDescription)
                .lines(newLines == null ? List.of() : List.copyOf(newLines))
                .updatedAt(clockSupplier.get())
                .build();
    }

    /**
     * 承認申請を行う
     *
     * @return 承認待ち状態の JournalEntry
     * @throws IllegalStateException 下書き以外のステータスの場合
     */
    public JournalEntry submitForApproval() {
        if (status != JournalEntryStatus.DRAFT) {
            throw new IllegalStateException("下書き状態の仕訳のみ承認申請可能です");
        }
        return this.withStatus(JournalEntryStatus.PENDING);
    }

    /**
     * 仕訳を承認する
     *
     * @param approver 承認者
     * @param approvedAt 承認日時
     * @return 承認済み状態の JournalEntry
     * @throws IllegalStateException 承認待ち以外のステータスの場合
     */
    public JournalEntry approve(UserId approver, LocalDateTime approvedAt) {
        if (status != JournalEntryStatus.PENDING) {
            throw new IllegalStateException("承認待ち状態の仕訳のみ承認可能です");
        }
        if (approver == null) {
            throw new IllegalArgumentException("承認者は必須です");
        }
        return this.toBuilder()
                .status(JournalEntryStatus.APPROVED)
                .approvedBy(approver)
                .approvedAt(approvedAt)
                .build();
    }

    /**
     * 明細行を追加した新しい仕訳を返す
     *
     * @param line 追加する明細行
     * @return 明細行追加後の JournalEntry
     */
    public JournalEntry addLine(JournalEntryLine line) {
        if (line == null) {
            throw new IllegalArgumentException("明細行は必須です");
        }
        List<JournalEntryLine> updatedLines = new ArrayList<>(lines);
        updatedLines.add(line);
        return this.withLines(List.copyOf(updatedLines));
    }

    /**
     * 指定行番号の明細行を削除した新しい仕訳を返す
     *
     * @param lineNumber 行番号
     * @return 明細行削除後の JournalEntry
     */
    public JournalEntry removeLine(Integer lineNumber) {
        if (lineNumber == null) {
            throw new IllegalArgumentException("行番号は必須です");
        }
        List<JournalEntryLine> updatedLines = lines.stream()
                .filter(line -> !lineNumber.equals(line.lineNumber()))
                .toList();
        return this.withLines(List.copyOf(updatedLines));
    }

    /**
     * 借方合計金額を取得する
     *
     * @return 借方合計
     */
    public Money totalDebitAmount() {
        return totalAmount(true);
    }

    /**
     * 貸方合計金額を取得する
     *
     * @return 貸方合計
     */
    public Money totalCreditAmount() {
        return totalAmount(false);
    }

    /**
     * 貸借一致を判定する
     *
     * @return 貸借一致の場合 true
     */
    public boolean isBalanced() {
        return totalDebitAmount().value().compareTo(totalCreditAmount().value()) == 0;
    }

    /**
     * 保存前バリデーション
     */
    public void validateForSave() {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("仕訳明細は 1 行以上必要です");
        }
        if (!isBalanced()) {
            throw new IllegalArgumentException("貸借一致していません");
        }
    }

    private Money totalAmount(boolean debit) {
        return lines.stream()
                .map(line -> debit ? line.debitAmount() : line.creditAmount())
                .filter(Objects::nonNull)
                .reduce(Money.ZERO, Money::add);
    }

    private static List<JournalEntryLine> requireLines(List<JournalEntryLine> lines) {
        if (lines == null) {
            throw new IllegalArgumentException("明細は必須です");
        }
        return lines;
    }
}
