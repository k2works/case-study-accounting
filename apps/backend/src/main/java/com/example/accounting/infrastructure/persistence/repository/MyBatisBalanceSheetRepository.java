package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.application.port.out.BalanceSheetRepository;
import com.example.accounting.infrastructure.persistence.entity.BalanceSheetEntity;
import com.example.accounting.infrastructure.persistence.mapper.BalanceSheetMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public class MyBatisBalanceSheetRepository implements BalanceSheetRepository {
    private final BalanceSheetMapper balanceSheetMapper;

    public MyBatisBalanceSheetRepository(BalanceSheetMapper balanceSheetMapper) {
        this.balanceSheetMapper = balanceSheetMapper;
    }

    @Override
    public List<BalanceSheetEntity> findBalanceSheet(LocalDate date) {
        return balanceSheetMapper.findBalanceSheet(date);
    }
}
