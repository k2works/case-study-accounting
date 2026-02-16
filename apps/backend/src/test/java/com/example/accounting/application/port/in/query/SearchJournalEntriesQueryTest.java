package com.example.accounting.application.port.in.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SearchJournalEntriesQuery")
class SearchJournalEntriesQueryTest {

    @Nested
    @DisplayName("バリデーション")
    class Validation {

        @Test
        @DisplayName("page が負の場合はバリデーションエラーになる")
        void shouldReturnLeftWhenPageIsNegative() {
            assertThat(SearchJournalEntriesQuery.of(
                    -1, 20, List.of(), null, null, null, null, null, null).isLeft()).isTrue();
        }

        @Test
        @DisplayName("size が 0 の場合はバリデーションエラーになる")
        void shouldReturnLeftWhenSizeIsZero() {
            assertThat(SearchJournalEntriesQuery.of(
                    0, 0, List.of(), null, null, null, null, null, null).isLeft()).isTrue();
        }

        @Test
        @DisplayName("size が 101 の場合はバリデーションエラーになる")
        void shouldReturnLeftWhenSizeExceeds100() {
            assertThat(SearchJournalEntriesQuery.of(
                    0, 101, List.of(), null, null, null, null, null, null).isLeft()).isTrue();
        }

        @Test
        @DisplayName("statuses が null の場合は空リストに変換される")
        void shouldConvertNullStatusesToEmptyList() {
            SearchJournalEntriesQuery query = new SearchJournalEntriesQuery(
                    0, 20, null, null, null, null, null, null, null);

            assertThat(query.statuses()).isEmpty();
        }

        @Test
        @DisplayName("全パラメータを指定してクエリを作成できる")
        void shouldCreateQueryWithAllParams() {
            LocalDate dateFrom = LocalDate.of(2024, 1, 1);
            LocalDate dateTo = LocalDate.of(2024, 12, 31);
            List<String> statuses = List.of("DRAFT");
            BigDecimal amountFrom = new BigDecimal("1000");
            BigDecimal amountTo = new BigDecimal("50000");

            SearchJournalEntriesQuery query = new SearchJournalEntriesQuery(
                    0, 20, statuses, dateFrom, dateTo, 1, amountFrom, amountTo, "売上");

            assertThat(query.page()).isZero();
            assertThat(query.size()).isEqualTo(20);
            assertThat(query.statuses()).containsExactly("DRAFT");
            assertThat(query.dateFrom()).isEqualTo(dateFrom);
            assertThat(query.dateTo()).isEqualTo(dateTo);
            assertThat(query.accountId()).isEqualTo(1);
            assertThat(query.amountFrom()).isEqualByComparingTo("1000");
            assertThat(query.amountTo()).isEqualByComparingTo("50000");
            assertThat(query.description()).isEqualTo("売上");
        }
    }

    @Nested
    @DisplayName("ファクトリメソッド")
    class FactoryMethods {

        @Test
        @DisplayName("defaultQuery でデフォルトクエリを作成できる")
        void shouldCreateDefaultQuery() {
            SearchJournalEntriesQuery query = SearchJournalEntriesQuery.defaultQuery();

            assertThat(query.page()).isZero();
            assertThat(query.size()).isEqualTo(20);
            assertThat(query.statuses()).isEmpty();
            assertThat(query.dateFrom()).isNull();
            assertThat(query.dateTo()).isNull();
            assertThat(query.accountId()).isNull();
            assertThat(query.amountFrom()).isNull();
            assertThat(query.amountTo()).isNull();
            assertThat(query.description()).isNull();
        }
    }
}
