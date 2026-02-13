package com.example.accounting.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyAccountBalanceEntityTest {

    @Test
    void shouldSetAndGetAllFields() {
        MonthlyAccountBalanceEntity entity = new MonthlyAccountBalanceEntity();
        entity.setFiscalPeriod(2024);
        entity.setMonth(3);
        entity.setAccountCode("1100");
        entity.setOpeningBalance(new BigDecimal("10000"));
        entity.setDebitAmount(new BigDecimal("5000"));
        entity.setCreditAmount(new BigDecimal("3000"));
        entity.setClosingBalance(new BigDecimal("12000"));

        assertThat(entity.getFiscalPeriod()).isEqualTo(2024);
        assertThat(entity.getMonth()).isEqualTo(3);
        assertThat(entity.getAccountCode()).isEqualTo("1100");
        assertThat(entity.getOpeningBalance()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(entity.getDebitAmount()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(entity.getCreditAmount()).isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(entity.getClosingBalance()).isEqualByComparingTo(new BigDecimal("12000"));
    }

    @Test
    void shouldAllowNullValues() {
        MonthlyAccountBalanceEntity entity = new MonthlyAccountBalanceEntity();

        assertThat(entity.getFiscalPeriod()).isNull();
        assertThat(entity.getMonth()).isNull();
        assertThat(entity.getAccountCode()).isNull();
        assertThat(entity.getOpeningBalance()).isNull();
        assertThat(entity.getDebitAmount()).isNull();
        assertThat(entity.getCreditAmount()).isNull();
        assertThat(entity.getClosingBalance()).isNull();
    }
}
