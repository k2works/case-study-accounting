package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.infrastructure.persistence.entity.MonthlyAccountBalanceEntity;
import com.example.accounting.infrastructure.persistence.mapper.MonthlyAccountBalanceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyBatisMonthlyAccountBalanceRepositoryTest {

    @Mock
    private MonthlyAccountBalanceMapper mapper;

    private MyBatisMonthlyAccountBalanceRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MyBatisMonthlyAccountBalanceRepository(mapper);
    }

    @Test
    void shouldDelegateToMapper() {
        MonthlyAccountBalanceEntity entity = new MonthlyAccountBalanceEntity();
        entity.setAccountCode("1100");
        entity.setFiscalPeriod(2024);
        entity.setMonth(1);
        entity.setOpeningBalance(new BigDecimal("10000"));

        when(mapper.findByAccountCodeAndFiscalPeriod("1100", 2024))
                .thenReturn(List.of(entity));

        List<MonthlyAccountBalanceEntity> result =
                repository.findByAccountCodeAndFiscalPeriod("1100", 2024);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getAccountCode()).isEqualTo("1100");
        verify(mapper).findByAccountCodeAndFiscalPeriod("1100", 2024);
    }

    @Test
    void shouldDelegateWithNullFiscalPeriod() {
        when(mapper.findByAccountCodeAndFiscalPeriod("1100", null))
                .thenReturn(List.of());

        List<MonthlyAccountBalanceEntity> result =
                repository.findByAccountCodeAndFiscalPeriod("1100", null);

        assertThat(result).isEmpty();
        verify(mapper).findByAccountCodeAndFiscalPeriod("1100", null);
    }
}
