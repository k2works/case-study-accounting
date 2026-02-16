package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.application.port.out.GetDailyBalanceResult.DailyBalanceEntry;
import com.example.accounting.infrastructure.persistence.entity.DailyBalanceEntryEntity;
import com.example.accounting.infrastructure.persistence.mapper.JournalEntryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JournalEntryRepositoryImpl - DailyBalance")
class JournalEntryRepositoryImplDailyBalanceTest {

    @Mock
    private JournalEntryMapper journalEntryMapper;

    private JournalEntryRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new JournalEntryRepositoryImpl(journalEntryMapper);
    }

    @Nested
    @DisplayName("findDailyBalanceByAccountAndPeriod")
    class FindDailyBalanceByAccountAndPeriod {

        @Test
        @DisplayName("日次残高を取得できる")
        void shouldFindDailyBalance() {
            Integer accountId = 1;
            LocalDate dateFrom = LocalDate.of(2024, 1, 1);
            LocalDate dateTo = LocalDate.of(2024, 1, 31);

            DailyBalanceEntryEntity entity = new DailyBalanceEntryEntity();
            entity.setDate(LocalDate.of(2024, 1, 15));
            entity.setDebitTotal(new BigDecimal("1000"));
            entity.setCreditTotal(new BigDecimal("500"));
            entity.setTransactionCount(3L);

            when(journalEntryMapper.findDailyBalanceByAccountAndPeriod(accountId, dateFrom, dateTo))
                    .thenReturn(List.of(entity));

            List<DailyBalanceEntry> result = repository.findDailyBalanceByAccountAndPeriod(
                    accountId, dateFrom, dateTo)
                    .getOrElse(List.of());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(result.get(0).debitTotal()).isEqualByComparingTo("1000");
            assertThat(result.get(0).creditTotal()).isEqualByComparingTo("500");
            assertThat(result.get(0).transactionCount()).isEqualTo(3L);
        }

        @Test
        @DisplayName("transactionCount が null の場合は 0 になる")
        void shouldDefaultTransactionCountToZeroWhenNull() {
            Integer accountId = 2;
            LocalDate dateFrom = LocalDate.of(2024, 2, 1);
            LocalDate dateTo = LocalDate.of(2024, 2, 29);

            DailyBalanceEntryEntity entity = new DailyBalanceEntryEntity();
            entity.setDate(LocalDate.of(2024, 2, 10));
            entity.setDebitTotal(new BigDecimal("200"));
            entity.setCreditTotal(BigDecimal.ZERO);
            entity.setTransactionCount(null);

            when(journalEntryMapper.findDailyBalanceByAccountAndPeriod(accountId, dateFrom, dateTo))
                    .thenReturn(List.of(entity));

            List<DailyBalanceEntry> result = repository.findDailyBalanceByAccountAndPeriod(
                    accountId, dateFrom, dateTo)
                    .getOrElse(List.of());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).transactionCount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("空のリストを返す")
        void shouldReturnEmptyList() {
            Integer accountId = 3;
            LocalDate dateFrom = LocalDate.of(2024, 3, 1);
            LocalDate dateTo = LocalDate.of(2024, 3, 31);

            when(journalEntryMapper.findDailyBalanceByAccountAndPeriod(accountId, dateFrom, dateTo))
                    .thenReturn(List.of());

            List<DailyBalanceEntry> result = repository.findDailyBalanceByAccountAndPeriod(
                    accountId, dateFrom, dateTo)
                    .getOrElse(List.of());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("calculateBalanceBeforeDate")
    class CalculateBalanceBeforeDate {

        @Test
        @DisplayName("期首残高を計算できる")
        void shouldCalculateBalance() {
            Integer accountId = 1;
            LocalDate date = LocalDate.of(2024, 4, 1);

            when(journalEntryMapper.calculateBalanceBeforeDate(accountId, date))
                    .thenReturn(new BigDecimal("5000"));

            BigDecimal result = repository.calculateBalanceBeforeDate(accountId, date)
                    .getOrElse((BigDecimal) null);

            assertThat(result).isEqualByComparingTo("5000");
        }
    }
}
