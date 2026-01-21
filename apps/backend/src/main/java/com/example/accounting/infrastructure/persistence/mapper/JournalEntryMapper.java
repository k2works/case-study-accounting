package com.example.accounting.infrastructure.persistence.mapper;

import com.example.accounting.infrastructure.persistence.entity.JournalEntryEntity;
import com.example.accounting.infrastructure.persistence.entity.JournalEntryLineEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    void update(JournalEntryEntity entity);

    void deleteLines(@Param("journalEntryId") Integer journalEntryId);

    Optional<JournalEntryEntity> findById(Integer id);

    List<JournalEntryEntity> findAll();

    List<JournalEntryLineEntity> findLinesByJournalEntryId(@Param("journalEntryId") Integer journalEntryId);

    void deleteById(Integer id);
}
