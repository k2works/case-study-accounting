package com.example.accounting.application.service;

import com.example.accounting.application.port.in.query.SearchJournalEntriesQuery;
import com.example.accounting.application.port.out.GetJournalEntriesResult;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.application.port.out.JournalEntrySearchCriteria;
import com.example.accounting.application.service.journal.SearchJournalEntriesService;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;
import com.example.accounting.domain.model.journal.JournalEntryLine;
import com.example.accounting.domain.model.journal.JournalEntryStatus;
import com.example.accounting.domain.model.journal.Money;
import com.example.accounting.domain.model.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 仕訳検索サービスのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("仕訳検索サービス")
class SearchJournalEntriesServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private SearchJournalEntriesService searchJournalEntriesService;

    @BeforeEach
    void setUp() {
        searchJournalEntriesService = new SearchJournalEntriesService(journalEntryRepository);
    }

    @Nested
    @DisplayName("検索成功")
    class SuccessfulSearch {

        @Test
        @DisplayName("デフォルトクエリで全件検索できる（条件なし）")
        void shouldSearchAllByDefaultQuery() {
            SearchJournalEntriesQuery query = SearchJournalEntriesQuery.defaultQuery();
            JournalEntry entry1 = createEntry(1, "売上計上", 1, new BigDecimal("1000"));
            JournalEntry entry2 = createEntry(2, "交通費", 3, new BigDecimal("2000"));

            when(journalEntryRepository.countBySearchConditions(any(JournalEntrySearchCriteria.class)))
                    .thenReturn(2L);
            when(journalEntryRepository.searchByConditions(any(JournalEntrySearchCriteria.class)))
                    .thenReturn(List.of(entry1, entry2));

            GetJournalEntriesResult result = searchJournalEntriesService.execute(query);

            assertThat(result.content()).hasSize(2);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
            assertThat(result.totalElements()).isEqualTo(2L);
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.content().get(0).journalEntryId()).isEqualTo(1);
            assertThat(result.content().get(0).description()).isEqualTo("売上計上");
            assertThat(result.content().get(0).totalDebitAmount()).isEqualTo(new BigDecimal("1000"));
            assertThat(result.content().get(0).totalCreditAmount()).isEqualTo(new BigDecimal("1000"));
        }

        @Test
        @DisplayName("摘要で検索できる")
        void shouldSearchByDescription() {
            SearchJournalEntriesQuery query = new SearchJournalEntriesQuery(
                    0,
                    20,
                    List.of(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    "交通費"
            );
            JournalEntry entry = createEntry(3, "交通費", 2, new BigDecimal("3000"));

            when(journalEntryRepository.countBySearchConditions(any(JournalEntrySearchCriteria.class)))
                    .thenReturn(1L);
            when(journalEntryRepository.searchByConditions(any(JournalEntrySearchCriteria.class)))
                    .thenReturn(List.of(entry));

            GetJournalEntriesResult result = searchJournalEntriesService.execute(query);

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1L);
            assertThat(result.content().get(0).description()).isEqualTo("交通費");
        }

        @Test
        @DisplayName("勘定科目 ID で検索できる")
        void shouldSearchByAccountId() {
            SearchJournalEntriesQuery query = new SearchJournalEntriesQuery(
                    0,
                    20,
                    List.of(),
                    null,
                    null,
                    1,
                    null,
                    null,
                    null
            );
            JournalEntry entry = createEntry(4, "売上計上", 1, new BigDecimal("5000"));

            when(journalEntryRepository.countBySearchConditions(any(JournalEntrySearchCriteria.class)))
                    .thenReturn(1L);
            when(journalEntryRepository.searchByConditions(any(JournalEntrySearchCriteria.class)))
                    .thenReturn(List.of(entry));

            GetJournalEntriesResult result = searchJournalEntriesService.execute(query);

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1L);
            assertThat(result.content().get(0).journalEntryId()).isEqualTo(4);
        }

        @Test
        @DisplayName("金額範囲で検索できる")
        void shouldSearchByAmountRange() {
            SearchJournalEntriesQuery query = new SearchJournalEntriesQuery(
                    0,
                    20,
                    List.of(),
                    null,
                    null,
                    null,
                    new BigDecimal("1000"),
                    new BigDecimal("5000"),
                    null
            );
            JournalEntry entry = createEntry(5, "消耗品費", 4, new BigDecimal("3000"));

            when(journalEntryRepository.countBySearchConditions(any(JournalEntrySearchCriteria.class)))
                    .thenReturn(1L);
            when(journalEntryRepository.searchByConditions(any(JournalEntrySearchCriteria.class)))
                    .thenReturn(List.of(entry));

            GetJournalEntriesResult result = searchJournalEntriesService.execute(query);

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1L);
            assertThat(result.content().get(0).description()).isEqualTo("消耗品費");
        }

        @Test
        @DisplayName("複数条件を組み合わせて検索できる")
        void shouldSearchByMultipleConditions() {
            SearchJournalEntriesQuery query = new SearchJournalEntriesQuery(
                    0,
                    20,
                    List.of(JournalEntryStatus.DRAFT.name()),
                    LocalDate.of(2024, 4, 1),
                    LocalDate.of(2024, 4, 30),
                    1,
                    new BigDecimal("1000"),
                    new BigDecimal("3000"),
                    "売上"
            );
            JournalEntry entry = createEntry(6, "売上計上", 1, new BigDecimal("2000"));

            when(journalEntryRepository.countBySearchConditions(any(JournalEntrySearchCriteria.class)))
                    .thenReturn(1L);
            when(journalEntryRepository.searchByConditions(any(JournalEntrySearchCriteria.class)))
                    .thenReturn(List.of(entry));

            GetJournalEntriesResult result = searchJournalEntriesService.execute(query);

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1L);
            assertThat(result.content().get(0).journalEntryId()).isEqualTo(6);
            assertThat(result.content().get(0).status()).isEqualTo(JournalEntryStatus.DRAFT.name());
        }
    }

    @Nested
    @DisplayName("検索結果 0 件")
    class EmptySearchResult {

        @Test
        @DisplayName("条件に一致する仕訳がない場合、空結果を返す")
        void shouldReturnEmptyWhenNoMatch() {
            SearchJournalEntriesQuery query = new SearchJournalEntriesQuery(
                    0,
                    20,
                    List.of(),
                    null,
                    null,
                    99,
                    null,
                    null,
                    null
            );

            when(journalEntryRepository.countBySearchConditions(any(JournalEntrySearchCriteria.class)))
                    .thenReturn(0L);

            GetJournalEntriesResult result = searchJournalEntriesService.execute(query);

            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
            verify(journalEntryRepository, never()).searchByConditions(any(JournalEntrySearchCriteria.class));
        }
    }

    @Nested
    @DisplayName("ページネーション")
    class Pagination {

        @Test
        @DisplayName("2 ページ目を取得できる")
        void shouldGetSecondPage() {
            SearchJournalEntriesQuery query = new SearchJournalEntriesQuery(
                    1,
                    2,
                    List.of(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            JournalEntry entry = createEntry(7, "売上計上", 1, new BigDecimal("1000"));

            when(journalEntryRepository.countBySearchConditions(any(JournalEntrySearchCriteria.class)))
                    .thenReturn(5L);
            when(journalEntryRepository.searchByConditions(any(JournalEntrySearchCriteria.class)))
                    .thenReturn(List.of(entry));

            GetJournalEntriesResult result = searchJournalEntriesService.execute(query);

            assertThat(result.content()).hasSize(1);
            assertThat(result.page()).isEqualTo(1);
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.totalElements()).isEqualTo(5L);
            assertThat(result.totalPages()).isEqualTo(3);
        }
    }

    private JournalEntry createEntry(Integer id, String description, int accountId, BigDecimal amount) {
        return JournalEntry.reconstruct(
                JournalEntryId.of(id),
                LocalDate.of(2024, 4, 1),
                description,
                JournalEntryStatus.DRAFT,
                1,
                List.of(
                        JournalEntryLine.of(1, AccountId.of(accountId), Money.of(amount), null),
                        JournalEntryLine.of(2, AccountId.of(2), null, Money.of(amount))
                ),
                UserId.of("user-1"),
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2024, 4, 1, 10, 0),
                LocalDateTime.of(2024, 4, 1, 10, 0)
        );
    }
}
