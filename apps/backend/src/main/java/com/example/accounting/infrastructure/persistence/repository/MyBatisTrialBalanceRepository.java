package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.application.port.out.TrialBalanceRepository;
import com.example.accounting.infrastructure.persistence.entity.TrialBalanceEntity;
import com.example.accounting.infrastructure.persistence.mapper.TrialBalanceMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public class MyBatisTrialBalanceRepository implements TrialBalanceRepository {
    private final TrialBalanceMapper trialBalanceMapper;

    public MyBatisTrialBalanceRepository(TrialBalanceMapper trialBalanceMapper) {
        this.trialBalanceMapper = trialBalanceMapper;
    }

    @Override
    public List<TrialBalanceEntity> findTrialBalance(LocalDate date) {
        return trialBalanceMapper.findTrialBalance(date);
    }
}
