package com.example.accounting.application.port.out;

import com.example.accounting.infrastructure.persistence.entity.MonthlyAccountBalanceEntity;
import java.util.List;

public interface MonthlyAccountBalanceRepository {
    List<MonthlyAccountBalanceEntity> findByAccountCodeAndFiscalPeriod(
            String accountCode, Integer fiscalPeriod);
}
