package com.example.accounting.application.port.in.query;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class GetTrialBalanceQueryTest {

    @Test
    void shouldCreateWithDate() {
        LocalDate date = LocalDate.of(2024, 6, 30);
        GetTrialBalanceQuery query = new GetTrialBalanceQuery(date);

        assertThat(query.date()).isEqualTo(date);
    }

    @Test
    void shouldAllowNullDate() {
        GetTrialBalanceQuery query = new GetTrialBalanceQuery(null);

        assertThat(query.date()).isNull();
    }
}
