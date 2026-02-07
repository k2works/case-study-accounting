package com.example.accounting.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 日次残高集計エンティティ（永続化用）
 */
public class DailyBalanceEntryEntity {

    private LocalDate date;
    private BigDecimal debitTotal;
    private BigDecimal creditTotal;
    private Long transactionCount;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getDebitTotal() {
        return debitTotal;
    }

    public void setDebitTotal(BigDecimal debitTotal) {
        this.debitTotal = debitTotal;
    }

    public BigDecimal getCreditTotal() {
        return creditTotal;
    }

    public void setCreditTotal(BigDecimal creditTotal) {
        this.creditTotal = creditTotal;
    }

    public Long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Long transactionCount) {
        this.transactionCount = transactionCount;
    }
}
