package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.journal.JournalEntryLine;
import com.example.accounting.domain.model.journal.Money;

import java.math.BigDecimal;

/**
 * 仕訳明細行エンティティ（永続化用）
 */
public class JournalEntryLineEntity {

    private Integer id;
    private Integer journalEntryId;
    private Integer lineNumber;
    private Integer accountId;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;

    /**
     * ドメインモデルからエンティティを生成する
     */
    public static JournalEntryLineEntity fromDomain(JournalEntryLine line, Integer journalEntryId) {
        JournalEntryLineEntity entity = new JournalEntryLineEntity();
        entity.journalEntryId = journalEntryId;
        entity.lineNumber = line.lineNumber();
        entity.accountId = line.accountId().value();
        entity.debitAmount = line.debitAmount() != null ? line.debitAmount().value() : BigDecimal.ZERO;
        entity.creditAmount = line.creditAmount() != null ? line.creditAmount().value() : BigDecimal.ZERO;
        return entity;
    }

    /**
     * エンティティからドメインモデルを再構築する
     */
    public JournalEntryLine toDomain() {
        Money debit = debitAmount != null && debitAmount.compareTo(BigDecimal.ZERO) > 0
                ? Money.reconstruct(debitAmount)
                : null;
        Money credit = creditAmount != null && creditAmount.compareTo(BigDecimal.ZERO) > 0
                ? Money.reconstruct(creditAmount)
                : null;
        return JournalEntryLine.reconstruct(
                lineNumber,
                AccountId.of(accountId),
                debit,
                credit
        );
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getJournalEntryId() {
        return journalEntryId;
    }

    public void setJournalEntryId(Integer journalEntryId) {
        this.journalEntryId = journalEntryId;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(BigDecimal debitAmount) {
        this.debitAmount = debitAmount;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }
}
