package com.example.accounting.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProfitAndLossEntityTest {

    @Test
    void shouldSetAndGetAllFields() {
        ProfitAndLossEntity entity = new ProfitAndLossEntity();
        entity.setAccountCode("4000");
        entity.setAccountName("売上高");
        entity.setAccountType("REVENUE");
        entity.setTotalDebit(new BigDecimal("10000"));
        entity.setTotalCredit(new BigDecimal("50000"));
        entity.setAmount(new BigDecimal("40000"));

        assertThat(entity.getAccountCode()).isEqualTo("4000");
        assertThat(entity.getAccountName()).isEqualTo("売上高");
        assertThat(entity.getAccountType()).isEqualTo("REVENUE");
        assertThat(entity.getTotalDebit()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(entity.getTotalCredit()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(entity.getAmount()).isEqualByComparingTo(new BigDecimal("40000"));
    }

    @Test
    void shouldAllowNullValues() {
        ProfitAndLossEntity entity = new ProfitAndLossEntity();

        assertThat(entity.getAccountCode()).isNull();
        assertThat(entity.getAccountName()).isNull();
        assertThat(entity.getAccountType()).isNull();
        assertThat(entity.getTotalDebit()).isNull();
        assertThat(entity.getTotalCredit()).isNull();
        assertThat(entity.getAmount()).isNull();
    }
}
