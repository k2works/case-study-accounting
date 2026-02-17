package com.example.accounting.infrastructure.persistence.entity;

import java.math.BigDecimal;

/**
 * 財務諸表エンティティ基底クラス（MyBatis マッピング用）
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class FinancialStatementEntity {
    private String accountCode;
    private String accountName;
    private String accountType;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getTotalDebit() {
        return totalDebit;
    }

    public void setTotalDebit(BigDecimal totalDebit) {
        this.totalDebit = totalDebit;
    }

    public BigDecimal getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(BigDecimal totalCredit) {
        this.totalCredit = totalCredit;
    }
}
