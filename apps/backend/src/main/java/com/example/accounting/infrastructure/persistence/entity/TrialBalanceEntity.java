package com.example.accounting.infrastructure.persistence.entity;

import java.math.BigDecimal;

/**
 * 試算表エンティティ（MyBatis マッピング用）
 */
public class TrialBalanceEntity {
    private String accountCode;
    private String accountName;
    private String bsplCategory;
    private String accountType;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private BigDecimal balance;

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

    public String getBsplCategory() {
        return bsplCategory;
    }

    public void setBsplCategory(String bsplCategory) {
        this.bsplCategory = bsplCategory;
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
