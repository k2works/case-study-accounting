package com.example.accounting.infrastructure.persistence.mapper;

import com.example.accounting.infrastructure.persistence.entity.BalanceSheetEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BalanceSheetMapper {
    List<BalanceSheetEntity> findBalanceSheet(@Param("date") LocalDate date);
}
