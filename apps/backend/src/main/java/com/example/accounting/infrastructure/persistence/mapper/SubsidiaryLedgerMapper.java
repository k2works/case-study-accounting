package com.example.accounting.infrastructure.persistence.mapper;

import com.example.accounting.infrastructure.persistence.entity.JournalEntryLineWithHeaderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 補助元帳 MyBatis Mapper
 *
 * <p>SQL 定義は mapper/SubsidiaryLedgerMapper.xml に記述</p>
 */
@Mapper
public interface SubsidiaryLedgerMapper {

    List<JournalEntryLineWithHeaderEntity> findPostedLinesByAccountAndSubAccountAndPeriod(
            @Param("accountCode") String accountCode,
            @Param("subAccountCode") String subAccountCode,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    long countPostedLinesByAccountAndSubAccountAndPeriod(
            @Param("accountCode") String accountCode,
            @Param("subAccountCode") String subAccountCode,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    BigDecimal calculateBalanceBeforeDateByAccountAndSubAccount(
            @Param("accountCode") String accountCode,
            @Param("subAccountCode") String subAccountCode,
            @Param("date") LocalDate date
    );
}
