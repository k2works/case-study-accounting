package com.example.accounting.infrastructure.persistence.mapper;

import com.example.accounting.infrastructure.persistence.entity.MonthlyAccountBalanceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MonthlyAccountBalanceMapper {
    List<MonthlyAccountBalanceEntity> findByAccountCodeAndFiscalPeriod(
            @Param("accountCode") String accountCode,
            @Param("fiscalPeriod") Integer fiscalPeriod);
}
