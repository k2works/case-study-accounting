package com.example.accounting.application.port.in.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GetDailyBalanceQuery")
class GetDailyBalanceQueryTest {

    @Nested
    @DisplayName("バリデーション")
    class Validation {

        @Test
        @DisplayName("accountId が null の場合は例外をスローする")
        void shouldThrowExceptionWhenAccountIdIsNull() {
            assertThatThrownBy(() -> new GetDailyBalanceQuery(null, null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("accountId must not be null");
        }

        @Test
        @DisplayName("有効なパラメータでクエリを作成できる")
        void shouldCreateQueryWithValidParams() {
            LocalDate dateFrom = LocalDate.of(2024, 1, 1);
            LocalDate dateTo = LocalDate.of(2024, 12, 31);

            GetDailyBalanceQuery query = new GetDailyBalanceQuery(10, dateFrom, dateTo);

            assertThat(query.accountId()).isEqualTo(10);
            assertThat(query.dateFrom()).isEqualTo(dateFrom);
            assertThat(query.dateTo()).isEqualTo(dateTo);
        }
    }
}
