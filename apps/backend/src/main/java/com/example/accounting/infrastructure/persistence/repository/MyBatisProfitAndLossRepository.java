package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.application.port.out.ProfitAndLossRepository;
import com.example.accounting.infrastructure.persistence.entity.ProfitAndLossEntity;
import com.example.accounting.infrastructure.persistence.mapper.ProfitAndLossMapper;
import io.vavr.control.Try;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class MyBatisProfitAndLossRepository implements ProfitAndLossRepository {
    private final ProfitAndLossMapper profitAndLossMapper;

    public MyBatisProfitAndLossRepository(ProfitAndLossMapper profitAndLossMapper) {
        this.profitAndLossMapper = profitAndLossMapper;
    }

    @Override
    public Try<List<ProfitAndLossEntity>> findProfitAndLoss(LocalDate dateFrom, LocalDate dateTo) {
        return Try.of(() -> profitAndLossMapper.findProfitAndLoss(dateFrom, dateTo));
    }
}
