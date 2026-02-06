package com.example.accounting.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 仕訳ヘッダ情報付き仕訳行エンティティ（永続化用）
 */
public class JournalEntryLineWithHeaderEntity {

    private Integer journalEntryId;
    private LocalDate journalDate;
    private String description;
    private String lineDescription;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;

    public Integer getJournalEntryId() {
        return journalEntryId;
    }

    public void setJournalEntryId(Integer journalEntryId) {
        this.journalEntryId = journalEntryId;
    }

    public LocalDate getJournalDate() {
        return journalDate;
    }

    public void setJournalDate(LocalDate journalDate) {
        this.journalDate = journalDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLineDescription() {
        return lineDescription;
    }

    public void setLineDescription(String lineDescription) {
        this.lineDescription = lineDescription;
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
