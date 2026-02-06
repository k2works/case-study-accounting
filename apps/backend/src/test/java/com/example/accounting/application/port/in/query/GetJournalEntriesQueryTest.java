package com.example.accounting.application.port.in.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GetJournalEntriesQuery")
class GetJournalEntriesQueryTest {

    @Nested
    @DisplayName("バリデーション")
    class Validation {

        @Test
        @DisplayName("page が負の場合は例外をスローする")
        void shouldThrowExceptionWhenPageIsNegative() {
            assertThatThrownBy(() -> new GetJournalEntriesQuery(-1, 20, List.of(), null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("page must be >= 0");
        }

        @Test
        @DisplayName("size が 0 の場合は例外をスローする")
        void shouldThrowExceptionWhenSizeIsZero() {
            assertThatThrownBy(() -> new GetJournalEntriesQuery(0, 0, List.of(), null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("size must be between 1 and 100");
        }

        @Test
        @DisplayName("size が 101 の場合は例外をスローする")
        void shouldThrowExceptionWhenSizeExceeds100() {
            assertThatThrownBy(() -> new GetJournalEntriesQuery(0, 101, List.of(), null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("size must be between 1 and 100");
        }

        @Test
        @DisplayName("statuses が null の場合は空リストに変換される")
        void shouldConvertNullStatusesToEmptyList() {
            GetJournalEntriesQuery query = new GetJournalEntriesQuery(0, 20, null, null, null);

            assertThat(query.statuses()).isEmpty();
        }

        @Test
        @DisplayName("有効なパラメータでクエリを作成できる")
        void shouldCreateQueryWithValidParams() {
            LocalDate dateFrom = LocalDate.of(2024, 1, 1);
            LocalDate dateTo = LocalDate.of(2024, 12, 31);
            List<String> statuses = List.of("DRAFT", "APPROVED");

            GetJournalEntriesQuery query = new GetJournalEntriesQuery(1, 50, statuses, dateFrom, dateTo);

            assertThat(query.page()).isEqualTo(1);
            assertThat(query.size()).isEqualTo(50);
            assertThat(query.statuses()).containsExactly("DRAFT", "APPROVED");
            assertThat(query.dateFrom()).isEqualTo(dateFrom);
            assertThat(query.dateTo()).isEqualTo(dateTo);
        }
    }

    @Nested
    @DisplayName("ファクトリメソッド")
    class FactoryMethods {

        @Test
        @DisplayName("defaultQuery でデフォルトクエリを作成できる")
        void shouldCreateDefaultQuery() {
            GetJournalEntriesQuery query = GetJournalEntriesQuery.defaultQuery();

            assertThat(query.page()).isZero();
            assertThat(query.size()).isEqualTo(20);
            assertThat(query.statuses()).isEmpty();
            assertThat(query.dateFrom()).isNull();
            assertThat(query.dateTo()).isNull();
        }

        @Test
        @DisplayName("ofPage でページ指定クエリを作成できる")
        void shouldCreateQueryWithPage() {
            GetJournalEntriesQuery query = GetJournalEntriesQuery.ofPage(2, 30);

            assertThat(query.page()).isEqualTo(2);
            assertThat(query.size()).isEqualTo(30);
            assertThat(query.statuses()).isEmpty();
            assertThat(query.dateFrom()).isNull();
            assertThat(query.dateTo()).isNull();
        }
    }
}
