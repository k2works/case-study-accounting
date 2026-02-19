package com.example.accounting.infrastructure.persistence.mapper;

import com.example.accounting.infrastructure.persistence.entity.AutoJournalPatternEntity;
import com.example.accounting.infrastructure.persistence.entity.AutoJournalPatternItemEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AutoJournalPatternMapper {
    void insert(AutoJournalPatternEntity entity);

    void insertItems(@Param("list") List<AutoJournalPatternItemEntity> items);

    int update(AutoJournalPatternEntity entity);

    void deleteItems(Long patternId);

    Optional<AutoJournalPatternEntity> findById(Long id);

    List<AutoJournalPatternEntity> findAll();

    void deleteById(Long id);

    boolean existsByCode(String patternCode);
}
