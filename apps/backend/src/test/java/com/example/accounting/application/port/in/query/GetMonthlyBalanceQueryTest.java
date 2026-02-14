package com.example.accounting.application.port.in.query;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetMonthlyBalanceQueryTest {

    @Test
    void shouldCreateWithAccountCodeAndFiscalPeriod() {
        GetMonthlyBalanceQuery query = new GetMonthlyBalanceQuery("1100", 2024);

        assertThat(query.accountCode()).isEqualTo("1100");
        assertThat(query.fiscalPeriod()).isEqualTo(2024);
    }

    @Test
    void shouldAllowNullFiscalPeriod() {
        GetMonthlyBalanceQuery query = new GetMonthlyBalanceQuery("1100", null);

        assertThat(query.accountCode()).isEqualTo("1100");
        assertThat(query.fiscalPeriod()).isNull();
    }

    @Test
    void shouldThrowWhenAccountCodeIsNull() {
        assertThatThrownBy(() -> new GetMonthlyBalanceQuery(null, 2024))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("accountCode must not be null");
    }
}
