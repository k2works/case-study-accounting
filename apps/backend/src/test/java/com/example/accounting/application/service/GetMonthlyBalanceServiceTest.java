package com.example.accounting.application.service;

import com.example.accounting.application.port.in.query.GetMonthlyBalanceQuery;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.GetMonthlyBalanceResult;
import com.example.accounting.application.port.out.GetMonthlyBalanceResult.MonthlyBalanceEntry;
import com.example.accounting.application.port.out.MonthlyAccountBalanceRepository;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
import com.example.accounting.infrastructure.persistence.entity.MonthlyAccountBalanceEntity;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMonthlyBalanceServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MonthlyAccountBalanceRepository monthlyAccountBalanceRepository;

    private GetMonthlyBalanceService service;

    private static final String ACCOUNT_CODE = "1100";
    private static final Account TEST_ACCOUNT = Account.reconstruct(
            new AccountId(1), AccountCode.of(ACCOUNT_CODE), "現金", AccountType.ASSET);

    @BeforeEach
    void setUp() {
        service = new GetMonthlyBalanceService(accountRepository, monthlyAccountBalanceRepository);
    }

    @Test
    void shouldReturnMonthlyBalanceWithEntries() {
        when(accountRepository.findByCode(ACCOUNT_CODE)).thenReturn(Try.success(Optional.of(TEST_ACCOUNT)));

        MonthlyAccountBalanceEntity jan = createEntity(1,
                new BigDecimal("10000"), new BigDecimal("5000"),
                new BigDecimal("3000"), new BigDecimal("12000"));
        MonthlyAccountBalanceEntity feb = createEntity(2,
                new BigDecimal("12000"), new BigDecimal("2000"),
                new BigDecimal("1000"), new BigDecimal("13000"));

        when(monthlyAccountBalanceRepository.findByAccountCodeAndFiscalPeriod(ACCOUNT_CODE, 2024))
                .thenReturn(Try.success(List.of(jan, feb)));

        GetMonthlyBalanceResult result = service.execute(
                new GetMonthlyBalanceQuery(ACCOUNT_CODE, 2024));

        assertThat(result.accountCode()).isEqualTo(ACCOUNT_CODE);
        assertThat(result.accountName()).isEqualTo("現金");
        assertThat(result.fiscalPeriod()).isEqualTo(2024);
        assertThat(result.entries()).hasSize(2);

        assertThat(result.debitTotal()).isEqualByComparingTo(new BigDecimal("7000"));
        assertThat(result.creditTotal()).isEqualByComparingTo(new BigDecimal("4000"));
        assertThat(result.openingBalance()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(result.closingBalance()).isEqualByComparingTo(new BigDecimal("13000"));
    }

    @Test
    void shouldReturnEmptyResultWhenNoData() {
        when(accountRepository.findByCode(ACCOUNT_CODE)).thenReturn(Try.success(Optional.of(TEST_ACCOUNT)));
        when(monthlyAccountBalanceRepository.findByAccountCodeAndFiscalPeriod(ACCOUNT_CODE, null))
                .thenReturn(Try.success(List.of()));

        GetMonthlyBalanceResult result = service.execute(
                new GetMonthlyBalanceQuery(ACCOUNT_CODE, null));

        assertThat(result.entries()).isEmpty();
        assertThat(result.debitTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.creditTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.openingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.closingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        when(accountRepository.findByCode(ACCOUNT_CODE)).thenReturn(Try.success(Optional.empty()));

        assertThatThrownBy(() -> service.execute(new GetMonthlyBalanceQuery(ACCOUNT_CODE, 2024)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("勘定科目が見つかりません");
    }

    @Test
    void shouldHandleNullAmountsWithDefaultZero() {
        when(accountRepository.findByCode(ACCOUNT_CODE)).thenReturn(Try.success(Optional.of(TEST_ACCOUNT)));

        MonthlyAccountBalanceEntity entityWithNulls = createEntity(1, null, null, null, null);

        when(monthlyAccountBalanceRepository.findByAccountCodeAndFiscalPeriod(ACCOUNT_CODE, 2024))
                .thenReturn(Try.success(List.of(entityWithNulls)));

        GetMonthlyBalanceResult result = service.execute(
                new GetMonthlyBalanceQuery(ACCOUNT_CODE, 2024));

        List<MonthlyBalanceEntry> entries = result.entries();
        assertThat(entries).hasSize(1);
        assertThat(entries.getFirst().openingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(entries.getFirst().debitAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(entries.getFirst().creditAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(entries.getFirst().closingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldPassFiscalPeriodToRepository() {
        when(accountRepository.findByCode(ACCOUNT_CODE)).thenReturn(Try.success(Optional.of(TEST_ACCOUNT)));
        when(monthlyAccountBalanceRepository.findByAccountCodeAndFiscalPeriod(ACCOUNT_CODE, 2025))
                .thenReturn(Try.success(List.of()));

        GetMonthlyBalanceResult result = service.execute(
                new GetMonthlyBalanceQuery(ACCOUNT_CODE, 2025));

        assertThat(result.fiscalPeriod()).isEqualTo(2025);
    }

    private MonthlyAccountBalanceEntity createEntity(int month, BigDecimal opening,
                                                      BigDecimal debit, BigDecimal credit,
                                                      BigDecimal closing) {
        MonthlyAccountBalanceEntity entity = new MonthlyAccountBalanceEntity();
        entity.setMonth(month);
        entity.setAccountCode(ACCOUNT_CODE);
        entity.setFiscalPeriod(2024);
        entity.setOpeningBalance(opening);
        entity.setDebitAmount(debit);
        entity.setCreditAmount(credit);
        entity.setClosingBalance(closing);
        return entity;
    }
}
