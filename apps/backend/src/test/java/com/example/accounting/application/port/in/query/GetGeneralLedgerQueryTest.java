package com.example.accounting.application.port.in.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GetGeneralLedgerQuery")
class GetGeneralLedgerQueryTest {

    @Nested
    @DisplayName("バリデーション")
    class Validation {

        @Test
        @DisplayName("accountId が null の場合は例外をスローする")
        void shouldThrowExceptionWhenAccountIdIsNull() {
            assertThatThrownBy(() -> new GetGeneralLedgerQuery(null, null, null, 0, 20))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("accountId must not be null");
        }

        @Test
        @DisplayName("page が負の場合は例外をスローする")
        void shouldThrowExceptionWhenPageIsNegative() {
            assertThatThrownBy(() -> new GetGeneralLedgerQuery(1, null, null, -1, 20))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("page must be >= 0");
        }

        @Test
        @DisplayName("size が 0 の場合は例外をスローする")
        void shouldThrowExceptionWhenSizeIsZero() {
            assertThatThrownBy(() -> new GetGeneralLedgerQuery(1, null, null, 0, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("size must be 1-100");
        }

        @Test
        @DisplayName("size が 101 の場合は例外をスローする")
        void shouldThrowExceptionWhenSizeExceeds100() {
            assertThatThrownBy(() -> new GetGeneralLedgerQuery(1, null, null, 0, 101))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("size must be 1-100");
        }

        @Test
        @DisplayName("有効なパラメータでクエリを作成できる")
        void shouldCreateQueryWithValidParams() {
            LocalDate dateFrom = LocalDate.of(2024, 1, 1);
            LocalDate dateTo = LocalDate.of(2024, 12, 31);

            GetGeneralLedgerQuery query = new GetGeneralLedgerQuery(10, dateFrom, dateTo, 1, 50);

            assertThat(query.accountId()).isEqualTo(10);
            assertThat(query.dateFrom()).isEqualTo(dateFrom);
            assertThat(query.dateTo()).isEqualTo(dateTo);
            assertThat(query.page()).isEqualTo(1);
            assertThat(query.size()).isEqualTo(50);
        }
    }
}
