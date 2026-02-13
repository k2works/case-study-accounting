package com.example.accounting.application.service;

import com.example.accounting.application.port.in.query.GetTrialBalanceQuery;
import com.example.accounting.application.port.out.GetTrialBalanceResult;
import com.example.accounting.application.port.out.GetTrialBalanceResult.CategorySubtotal;
import com.example.accounting.application.port.out.TrialBalanceRepository;
import com.example.accounting.infrastructure.persistence.entity.TrialBalanceEntity;
import org.junit.jupiter.api.BeforeEach;
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
class GetTrialBalanceServiceTest {

    @Mock
    private TrialBalanceRepository trialBalanceRepository;

    private GetTrialBalanceService service;

    @BeforeEach
    void setUp() {
        service = new GetTrialBalanceService(trialBalanceRepository);
    }

    @Test
    void shouldReturnBalancedTrueWhenDebitEqualsCredit() {
        TrialBalanceEntity asset = createEntity("100", "現金", "B", "ASSET",
                new BigDecimal("1000"), BigDecimal.ZERO, new BigDecimal("1000"));
        TrialBalanceEntity liability = createEntity("200", "買掛金", "B", "LIABILITY",
                BigDecimal.ZERO, new BigDecimal("1000"), new BigDecimal("-1000"));

        when(trialBalanceRepository.findTrialBalance(null)).thenReturn(List.of(asset, liability));

        GetTrialBalanceResult result = service.execute(new GetTrialBalanceQuery(null));

        assertThat(result.balanced()).isTrue();
        assertThat(result.difference()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalDebit()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(result.totalCredit()).isEqualByComparingTo(new BigDecimal("1000"));
    }

    @Test
    void shouldReturnBalancedFalseWhenDebitNotEqualsCredit() {
        TrialBalanceEntity asset = createEntity("100", "現金", "B", "ASSET",
                new BigDecimal("1500"), BigDecimal.ZERO, new BigDecimal("1500"));
        TrialBalanceEntity liability = createEntity("200", "買掛金", "B", "LIABILITY",
                BigDecimal.ZERO, new BigDecimal("1000"), new BigDecimal("-1000"));

        when(trialBalanceRepository.findTrialBalance(null)).thenReturn(List.of(asset, liability));

        GetTrialBalanceResult result = service.execute(new GetTrialBalanceQuery(null));

        assertThat(result.balanced()).isFalse();
        assertThat(result.difference()).isEqualByComparingTo(new BigDecimal("500"));
    }

    @Test
    void shouldCalculateCategorySubtotals() {
        TrialBalanceEntity cash = createEntity("100", "現金", "B", "ASSET",
                new BigDecimal("5000"), BigDecimal.ZERO, new BigDecimal("5000"));
        TrialBalanceEntity ar = createEntity("110", "売掛金", "B", "ASSET",
                new BigDecimal("3000"), BigDecimal.ZERO, new BigDecimal("3000"));
        TrialBalanceEntity ap = createEntity("200", "買掛金", "B", "LIABILITY",
                BigDecimal.ZERO, new BigDecimal("4000"), new BigDecimal("-4000"));
        TrialBalanceEntity revenue = createEntity("400", "売上", "P", "REVENUE",
                BigDecimal.ZERO, new BigDecimal("6000"), new BigDecimal("-6000"));
        TrialBalanceEntity expense = createEntity("500", "仕入", "P", "EXPENSE",
                new BigDecimal("2000"), BigDecimal.ZERO, new BigDecimal("2000"));

        when(trialBalanceRepository.findTrialBalance(null))
                .thenReturn(List.of(cash, ar, ap, revenue, expense));

        GetTrialBalanceResult result = service.execute(new GetTrialBalanceQuery(null));

        List<CategorySubtotal> subtotals = result.categorySubtotals();
        assertThat(subtotals).hasSize(5);

        CategorySubtotal assetSubtotal = subtotals.stream()
                .filter(s -> "ASSET".equals(s.accountType())).findFirst().orElseThrow();
        assertThat(assetSubtotal.debitSubtotal()).isEqualByComparingTo(new BigDecimal("8000"));
        assertThat(assetSubtotal.creditSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(assetSubtotal.accountTypeDisplayName()).isEqualTo("資産");

        CategorySubtotal liabilitySubtotal = subtotals.stream()
                .filter(s -> "LIABILITY".equals(s.accountType())).findFirst().orElseThrow();
        assertThat(liabilitySubtotal.creditSubtotal()).isEqualByComparingTo(new BigDecimal("4000"));

        CategorySubtotal revenueSubtotal = subtotals.stream()
                .filter(s -> "REVENUE".equals(s.accountType())).findFirst().orElseThrow();
        assertThat(revenueSubtotal.creditSubtotal()).isEqualByComparingTo(new BigDecimal("6000"));
    }

    @Test
    void shouldExecuteQueryWithSpecifiedDate() {
        LocalDate date = LocalDate.of(2026, 1, 31);
        when(trialBalanceRepository.findTrialBalance(date)).thenReturn(List.of());

        GetTrialBalanceResult result = service.execute(new GetTrialBalanceQuery(date));

        assertThat(result.date()).isEqualTo(date);
        assertThat(result.entries()).isEmpty();
        assertThat(result.balanced()).isTrue();
    }

    @Test
    void shouldReturnEmptyResultWhenNoData() {
        when(trialBalanceRepository.findTrialBalance(null)).thenReturn(List.of());

        GetTrialBalanceResult result = service.execute(new GetTrialBalanceQuery(null));

        assertThat(result.entries()).isEmpty();
        assertThat(result.totalDebit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalCredit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.balanced()).isTrue();
    }

    private TrialBalanceEntity createEntity(String code, String name, String bspl, String type,
                                             BigDecimal debit, BigDecimal credit, BigDecimal balance) {
        TrialBalanceEntity entity = new TrialBalanceEntity();
        entity.setAccountCode(code);
        entity.setAccountName(name);
        entity.setBsplCategory(bspl);
        entity.setAccountType(type);
        entity.setTotalDebit(debit);
        entity.setTotalCredit(credit);
        entity.setBalance(balance);
        return entity;
    }
}
