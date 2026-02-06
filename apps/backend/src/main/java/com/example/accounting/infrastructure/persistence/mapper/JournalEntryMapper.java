package com.example.accounting.infrastructure.persistence.mapper;

import com.example.accounting.application.port.out.JournalEntrySearchCriteria;
import com.example.accounting.infrastructure.persistence.entity.JournalEntryEntity;
import com.example.accounting.infrastructure.persistence.entity.JournalEntryLineEntity;
import com.example.accounting.infrastructure.persistence.entity.JournalEntryLineWithHeaderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 仕訳 MyBatis Mapper
 *
 * <p>SQL 定義は mapper/JournalEntryMapper.xml に記述</p>
 */
@Mapper
public interface JournalEntryMapper {

    void insert(JournalEntryEntity entity);

    void insertLines(List<JournalEntryLineEntity> lines);

    int update(JournalEntryEntity entity);

    void deleteLines(@Param("journalEntryId") Integer journalEntryId);

    Optional<JournalEntryEntity> findById(Integer id);

    List<JournalEntryEntity> findAll();

    List<JournalEntryLineEntity> findLinesByJournalEntryId(@Param("journalEntryId") Integer journalEntryId);

    void deleteById(Integer id);

    List<JournalEntryEntity> findByConditions(
            @Param("statuses") List<String> statuses,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    long countByConditions(
            @Param("statuses") List<String> statuses,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    List<JournalEntryEntity> searchByConditions(@Param("c") JournalEntrySearchCriteria criteria);

    long countBySearchConditions(@Param("c") JournalEntrySearchCriteria criteria);

    List<JournalEntryLineWithHeaderEntity> findPostedLinesByAccountAndPeriod(
            @Param("accountId") Integer accountId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    long countPostedLinesByAccountAndPeriod(
            @Param("accountId") Integer accountId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    BigDecimal calculateBalanceBeforeDate(
            @Param("accountId") Integer accountId,
            @Param("date") LocalDate date
    );
}
