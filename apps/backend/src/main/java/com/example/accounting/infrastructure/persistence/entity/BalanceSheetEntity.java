package com.example.accounting.infrastructure.persistence.entity;

import java.math.BigDecimal;

/**
 * 貸借対照表エンティティ（MyBatis マッピング用）
 */
public class BalanceSheetEntity extends FinancialStatementEntity {
    private BigDecimal balance;

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
