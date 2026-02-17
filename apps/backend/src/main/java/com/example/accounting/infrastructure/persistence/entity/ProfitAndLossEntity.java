package com.example.accounting.infrastructure.persistence.entity;

import java.math.BigDecimal;

/**
 * 損益計算書エンティティ（MyBatis マッピング用）
 */
public class ProfitAndLossEntity extends FinancialStatementEntity {
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
