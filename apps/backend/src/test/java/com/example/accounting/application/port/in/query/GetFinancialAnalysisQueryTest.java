package com.example.accounting.application.port.in.query;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class GetFinancialAnalysisQueryTest {

    @Test
    void shouldCreateWithAllDates() {
        LocalDate dateFrom = LocalDate.of(2024, 1, 1);
        LocalDate dateTo = LocalDate.of(2024, 12, 31);
        LocalDate comparativeDateFrom = LocalDate.of(2023, 1, 1);
        LocalDate comparativeDateTo = LocalDate.of(2023, 12, 31);

        GetFinancialAnalysisQuery query = new GetFinancialAnalysisQuery(
                dateFrom,
                dateTo,
                comparativeDateFrom,
                comparativeDateTo
        );

        assertThat(query.dateFrom()).isEqualTo(dateFrom);
        assertThat(query.dateTo()).isEqualTo(dateTo);
        assertThat(query.comparativeDateFrom()).isEqualTo(comparativeDateFrom);
        assertThat(query.comparativeDateTo()).isEqualTo(comparativeDateTo);
    }

    @Test
    void shouldAllowNullDates() {
        GetFinancialAnalysisQuery query = new GetFinancialAnalysisQuery(null, null, null, null);

        assertThat(query.dateFrom()).isNull();
        assertThat(query.dateTo()).isNull();
        assertThat(query.comparativeDateFrom()).isNull();
        assertThat(query.comparativeDateTo()).isNull();
    }
}
