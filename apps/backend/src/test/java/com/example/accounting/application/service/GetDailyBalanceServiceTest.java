package com.example.accounting.application.service;

import com.example.accounting.application.port.in.query.GetDailyBalanceQuery;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.GetDailyBalanceResult;
import com.example.accounting.application.port.out.GetDailyBalanceResult.DailyBalanceEntry;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetDailyBalanceService")
class GetDailyBalanceServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private GetDailyBalanceService service;

    @BeforeEach
    void setUp() {
        service = new GetDailyBalanceService(accountRepository, journalEntryRepository);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("資産勘定は借方増加として日次残高を計算できる")
        void shouldCalculateDailyBalancesForDebitAccount() {
            LocalDate dateFrom = LocalDate.of(2024, 1, 1);
            LocalDate dateTo = LocalDate.of(2024, 1, 31);
            GetDailyBalanceQuery query = new GetDailyBalanceQuery(1, dateFrom, dateTo);

            Account account = Account.reconstruct(
                    AccountId.of(1), AccountCode.of("1101"), "現金", AccountType.ASSET);

            when(accountRepository.findById(AccountId.of(1)))
                    .thenReturn(Optional.of(account));
            when(journalEntryRepository.calculateBalanceBeforeDate(1, dateFrom))
                    .thenReturn(new BigDecimal("1000"));
            when(journalEntryRepository.findDailyBalanceByAccountAndPeriod(1, dateFrom, dateTo))
                    .thenReturn(List.of(
                            new DailyBalanceEntry(
                                    LocalDate.of(2024, 1, 10),
                                    new BigDecimal("500"),
                                    BigDecimal.ZERO,
                                    null,
                                    1L
                            ),
                            new DailyBalanceEntry(
                                    LocalDate.of(2024, 1, 20),
                                    BigDecimal.ZERO,
                                    new BigDecimal("200"),
                                    null,
                                    1L
                            )
                    ));

            GetDailyBalanceResult result = service.execute(query);

            assertThat(result.accountId()).isEqualTo(1);
            assertThat(result.accountCode()).isEqualTo("1101");
            assertThat(result.accountName()).isEqualTo("現金");
            assertThat(result.openingBalance()).isEqualByComparingTo("1000");
            assertThat(result.debitTotal()).isEqualByComparingTo("500");
            assertThat(result.creditTotal()).isEqualByComparingTo("200");
            assertThat(result.closingBalance()).isEqualByComparingTo("1300");
            assertThat(result.entries()).hasSize(2);
            assertThat(result.entries().get(0).balance()).isEqualByComparingTo("1500");
            assertThat(result.entries().get(1).balance()).isEqualByComparingTo("1300");
        }

        @Test
        @DisplayName("負債勘定は貸方増加として日次残高を計算できる")
        void shouldCalculateDailyBalancesForCreditAccount() {
            LocalDate dateFrom = LocalDate.of(2024, 4, 1);
            LocalDate dateTo = LocalDate.of(2024, 4, 30);
            GetDailyBalanceQuery query = new GetDailyBalanceQuery(2, dateFrom, dateTo);

            Account account = Account.reconstruct(
                    AccountId.of(2), AccountCode.of("2101"), "買掛金", AccountType.LIABILITY);

            when(accountRepository.findById(AccountId.of(2)))
                    .thenReturn(Optional.of(account));
            when(journalEntryRepository.calculateBalanceBeforeDate(2, dateFrom))
                    .thenReturn(new BigDecimal("-300"));
            when(journalEntryRepository.findDailyBalanceByAccountAndPeriod(2, dateFrom, dateTo))
                    .thenReturn(List.of(
                            new DailyBalanceEntry(
                                    LocalDate.of(2024, 4, 15),
                                    new BigDecimal("100"),
                                    new BigDecimal("400"),
                                    null,
                                    1L
                            )
                    ));

            GetDailyBalanceResult result = service.execute(query);

            assertThat(result.openingBalance()).isEqualByComparingTo("300");
            assertThat(result.debitTotal()).isEqualByComparingTo("100");
            assertThat(result.creditTotal()).isEqualByComparingTo("400");
            assertThat(result.closingBalance()).isEqualByComparingTo("600");
            assertThat(result.entries().get(0).balance()).isEqualByComparingTo("600");
        }
    }
}
