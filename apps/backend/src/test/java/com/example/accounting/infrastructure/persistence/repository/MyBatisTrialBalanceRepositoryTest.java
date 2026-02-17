package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.infrastructure.persistence.entity.TrialBalanceEntity;
import com.example.accounting.infrastructure.persistence.mapper.TrialBalanceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyBatisTrialBalanceRepositoryTest {

    @Mock
    private TrialBalanceMapper trialBalanceMapper;

    private MyBatisTrialBalanceRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MyBatisTrialBalanceRepository(trialBalanceMapper);
    }

    @Test
    void shouldDelegateToMapperWithDate() {
        LocalDate date = LocalDate.of(2024, 6, 30);
        TrialBalanceEntity entity = new TrialBalanceEntity();
        entity.setAccountCode("1000");
        entity.setAccountName("現金");
        entity.setBalance(new BigDecimal("50000"));

        when(trialBalanceMapper.findTrialBalance(date)).thenReturn(List.of(entity));

        List<TrialBalanceEntity> result = repository.findTrialBalance(date)
                .getOrElse(List.of());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getAccountCode()).isEqualTo("1000");
        verify(trialBalanceMapper).findTrialBalance(date);
    }

    @Test
    void shouldDelegateToMapperWithNullDate() {
        when(trialBalanceMapper.findTrialBalance(null)).thenReturn(List.of());

        List<TrialBalanceEntity> result = repository.findTrialBalance(null)
                .getOrElse(List.of());

        assertThat(result).isEmpty();
        verify(trialBalanceMapper).findTrialBalance(null);
    }
}
