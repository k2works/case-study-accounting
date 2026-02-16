package com.example.accounting.application.port.in.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GetSubsidiaryLedgerQuery")
class GetSubsidiaryLedgerQueryTest {

    @Nested
    @DisplayName("バリデーション")
    class Validation {

        @Test
        @DisplayName("accountCode が null の場合は例外をスローする")
        void shouldThrowExceptionWhenAccountCodeIsNull() {
            assertThatThrownBy(() -> new GetSubsidiaryLedgerQuery(null, null, null, null, 0, 20))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("accountCode must not be null");
        }

        @Test
        @DisplayName("page が負の場合は例外をスローする")
        void shouldThrowExceptionWhenPageIsNegative() {
            assertThatThrownBy(() -> new GetSubsidiaryLedgerQuery("1100", null, null, null, -1, 20))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("page must be >= 0");
        }

        @Test
        @DisplayName("size が 0 の場合は例外をスローする")
        void shouldThrowExceptionWhenSizeIsZero() {
            assertThatThrownBy(() -> new GetSubsidiaryLedgerQuery("1100", null, null, null, 0, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("size must be 1-100");
        }

        @Test
        @DisplayName("size が 101 の場合は例外をスローする")
        void shouldThrowExceptionWhenSizeExceeds100() {
            assertThatThrownBy(() -> new GetSubsidiaryLedgerQuery("1100", null, null, null, 0, 101))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("size must be 1-100");
        }

        @Test
        @DisplayName("有効なパラメータでクエリを作成できる")
        void shouldCreateQueryWithValidParams() {
            LocalDate dateFrom = LocalDate.of(2024, 4, 1);
            LocalDate dateTo = LocalDate.of(2024, 4, 30);

            GetSubsidiaryLedgerQuery query = new GetSubsidiaryLedgerQuery(
                    "1100", "001", dateFrom, dateTo, 1, 50);

            assertThat(query.accountCode()).isEqualTo("1100");
            assertThat(query.subAccountCode()).isEqualTo("001");
            assertThat(query.dateFrom()).isEqualTo(dateFrom);
            assertThat(query.dateTo()).isEqualTo(dateTo);
            assertThat(query.page()).isEqualTo(1);
            assertThat(query.size()).isEqualTo(50);
        }

        @Test
        @DisplayName("オプションフィールドが null でもクエリを作成できる")
        void shouldCreateQueryWithNullOptionalFields() {
            GetSubsidiaryLedgerQuery query = new GetSubsidiaryLedgerQuery(
                    "1100", null, null, null, 0, 20);

            assertThat(query.accountCode()).isEqualTo("1100");
            assertThat(query.subAccountCode()).isNull();
            assertThat(query.dateFrom()).isNull();
            assertThat(query.dateTo()).isNull();
            assertThat(query.page()).isZero();
            assertThat(query.size()).isEqualTo(20);
        }
    }
}
