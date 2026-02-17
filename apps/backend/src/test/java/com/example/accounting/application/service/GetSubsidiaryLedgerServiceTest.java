package com.example.accounting.application.service;

import com.example.accounting.application.port.in.query.GetSubsidiaryLedgerQuery;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.GetSubsidiaryLedgerResult;
import com.example.accounting.application.port.out.GetSubsidiaryLedgerResult.SubsidiaryLedgerEntry;
import com.example.accounting.application.port.out.SubsidiaryLedgerRepository;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
import io.vavr.control.Try;
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
@DisplayName("GetSubsidiaryLedgerService")
class GetSubsidiaryLedgerServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SubsidiaryLedgerRepository subsidiaryLedgerRepository;

    private GetSubsidiaryLedgerService service;

    @BeforeEach
    void setUp() {
        service = new GetSubsidiaryLedgerService(accountRepository, subsidiaryLedgerRepository);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("資産勘定は借方増加として残高と累計を計算できる")
        void shouldCalculateBalancesForDebitAccount() {
            LocalDate dateFrom = LocalDate.of(2024, 1, 1);
            LocalDate dateTo = LocalDate.of(2024, 1, 31);
            GetSubsidiaryLedgerQuery query = new GetSubsidiaryLedgerQuery(
                    "1101",
                    "S001",
                    dateFrom,
                    dateTo,
                    0,
                    20
            );

            Account account = Account.reconstruct(
                    AccountId.of(1), AccountCode.of("1101"), "現金", AccountType.ASSET);

            when(accountRepository.findByCode("1101"))
                    .thenReturn(Try.success(Optional.of(account)));
            when(subsidiaryLedgerRepository.calculateBalanceBeforeDateByAccountAndSubAccount(
                    "1101", "S001", dateFrom))
                    .thenReturn(Try.success(new BigDecimal("1000")));
            when(subsidiaryLedgerRepository.countPostedLinesByAccountAndSubAccountAndPeriod(
                    "1101", "S001", dateFrom, dateTo))
                    .thenReturn(Try.success(2L));
            when(subsidiaryLedgerRepository.findPostedLinesByAccountAndSubAccountAndPeriod(
                    "1101", "S001", dateFrom, dateTo, 0, 20))
                    .thenReturn(Try.success(List.of(
                            new SubsidiaryLedgerEntry(
                                    10,
                                    LocalDate.of(2024, 1, 10),
                                    "売上計上",
                                    new BigDecimal("500"),
                                    BigDecimal.ZERO,
                                    null
                            ),
                            new SubsidiaryLedgerEntry(
                                    11,
                                    LocalDate.of(2024, 1, 20),
                                    "支払",
                                    BigDecimal.ZERO,
                                    new BigDecimal("200"),
                                    null
                            )
                    )));

            GetSubsidiaryLedgerResult result = service.execute(query);

            assertThat(result.accountCode()).isEqualTo("1101");
            assertThat(result.accountName()).isEqualTo("現金");
            assertThat(result.subAccountCode()).isEqualTo("S001");
            assertThat(result.openingBalance()).isEqualByComparingTo("1000");
            assertThat(result.debitTotal()).isEqualByComparingTo("500");
            assertThat(result.creditTotal()).isEqualByComparingTo("200");
            assertThat(result.closingBalance()).isEqualByComparingTo("1300");
            assertThat(result.content()).hasSize(2);
            assertThat(result.content().get(0).runningBalance()).isEqualByComparingTo("1500");
            assertThat(result.content().get(1).runningBalance()).isEqualByComparingTo("1300");
            assertThat(result.totalElements()).isEqualTo(2L);
            assertThat(result.totalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("負債勘定は貸方増加として残高と累計を計算できる")
        void shouldCalculateBalancesForCreditAccount() {
            LocalDate dateFrom = LocalDate.of(2024, 4, 1);
            LocalDate dateTo = LocalDate.of(2024, 4, 30);
            GetSubsidiaryLedgerQuery query = new GetSubsidiaryLedgerQuery(
                    "2101",
                    null,
                    dateFrom,
                    dateTo,
                    0,
                    20
            );

            Account account = Account.reconstruct(
                    AccountId.of(2), AccountCode.of("2101"), "買掛金", AccountType.LIABILITY);

            when(accountRepository.findByCode("2101"))
                    .thenReturn(Try.success(Optional.of(account)));
            when(subsidiaryLedgerRepository.calculateBalanceBeforeDateByAccountAndSubAccount(
                    "2101", null, dateFrom))
                    .thenReturn(Try.success(new BigDecimal("-300")));
            when(subsidiaryLedgerRepository.countPostedLinesByAccountAndSubAccountAndPeriod(
                    "2101", null, dateFrom, dateTo))
                    .thenReturn(Try.success(1L));
            when(subsidiaryLedgerRepository.findPostedLinesByAccountAndSubAccountAndPeriod(
                    "2101", null, dateFrom, dateTo, 0, 20))
                    .thenReturn(Try.success(List.of(
                            new SubsidiaryLedgerEntry(
                                    21,
                                    LocalDate.of(2024, 4, 15),
                                    "仕入計上",
                                    new BigDecimal("100"),
                                    new BigDecimal("400"),
                                    null
                            )
                    )));

            GetSubsidiaryLedgerResult result = service.execute(query);

            assertThat(result.openingBalance()).isEqualByComparingTo("300");
            assertThat(result.debitTotal()).isEqualByComparingTo("100");
            assertThat(result.creditTotal()).isEqualByComparingTo("400");
            assertThat(result.closingBalance()).isEqualByComparingTo("600");
            assertThat(result.content().get(0).runningBalance()).isEqualByComparingTo("600");
        }
    }
}
