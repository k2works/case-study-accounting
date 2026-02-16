package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.application.port.out.MonthlyAccountBalanceRepository;
import com.example.accounting.infrastructure.persistence.entity.MonthlyAccountBalanceEntity;
import com.example.accounting.infrastructure.persistence.mapper.MonthlyAccountBalanceMapper;
import io.vavr.control.Try;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class MyBatisMonthlyAccountBalanceRepository implements MonthlyAccountBalanceRepository {

    private final MonthlyAccountBalanceMapper mapper;

    public MyBatisMonthlyAccountBalanceRepository(MonthlyAccountBalanceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Try<List<MonthlyAccountBalanceEntity>> findByAccountCodeAndFiscalPeriod(
            String accountCode, Integer fiscalPeriod) {
        return Try.of(() -> mapper.findByAccountCodeAndFiscalPeriod(accountCode, fiscalPeriod));
    }
}
