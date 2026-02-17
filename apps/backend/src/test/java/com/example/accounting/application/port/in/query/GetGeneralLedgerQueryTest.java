package com.example.accounting.application.port.in.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GetGeneralLedgerQuery")
class GetGeneralLedgerQueryTest {

    @Nested
    @DisplayName("バリデーション")
    class Validation {

        @Test
        @DisplayName("accountId が null の場合はバリデーションエラーになる")
        void shouldReturnLeftWhenAccountIdIsNull() {
            assertThat(GetGeneralLedgerQuery.of(null, null, null, 0, 20).getLeft())
                    .isEqualTo("勘定科目 ID は必須です");
        }

        @Test
        @DisplayName("page が負の場合はバリデーションエラーになる")
        void shouldReturnLeftWhenPageIsNegative() {
            assertThat(GetGeneralLedgerQuery.of(1, null, null, -1, 20).isLeft()).isTrue();
        }

        @Test
        @DisplayName("size が 0 の場合はバリデーションエラーになる")
        void shouldReturnLeftWhenSizeIsZero() {
            assertThat(GetGeneralLedgerQuery.of(1, null, null, 0, 0).isLeft()).isTrue();
        }

        @Test
        @DisplayName("size が 101 の場合はバリデーションエラーになる")
        void shouldReturnLeftWhenSizeExceeds100() {
            assertThat(GetGeneralLedgerQuery.of(1, null, null, 0, 101).isLeft()).isTrue();
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
