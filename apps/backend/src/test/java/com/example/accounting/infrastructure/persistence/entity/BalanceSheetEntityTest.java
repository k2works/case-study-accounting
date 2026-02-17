package com.example.accounting.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BalanceSheetEntityTest {

    @Test
    void shouldSetAndGetAllFields() {
        BalanceSheetEntity entity = new BalanceSheetEntity();
        entity.setAccountCode("1000");
        entity.setAccountName("現金");
        entity.setAccountType("ASSET");
        entity.setTotalDebit(new BigDecimal("50000"));
        entity.setTotalCredit(new BigDecimal("10000"));
        entity.setBalance(new BigDecimal("40000"));

        assertThat(entity.getAccountCode()).isEqualTo("1000");
        assertThat(entity.getAccountName()).isEqualTo("現金");
        assertThat(entity.getAccountType()).isEqualTo("ASSET");
        assertThat(entity.getTotalDebit()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(entity.getTotalCredit()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(entity.getBalance()).isEqualByComparingTo(new BigDecimal("40000"));
    }

    @Test
    void shouldAllowNullValues() {
        BalanceSheetEntity entity = new BalanceSheetEntity();

        assertThat(entity.getAccountCode()).isNull();
        assertThat(entity.getAccountName()).isNull();
        assertThat(entity.getAccountType()).isNull();
        assertThat(entity.getTotalDebit()).isNull();
        assertThat(entity.getTotalCredit()).isNull();
        assertThat(entity.getBalance()).isNull();
    }
}
