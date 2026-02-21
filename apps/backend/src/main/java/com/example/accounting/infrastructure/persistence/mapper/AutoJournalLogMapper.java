package com.example.accounting.infrastructure.persistence.mapper;

import com.example.accounting.infrastructure.persistence.entity.AutoJournalLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AutoJournalLogMapper {
    void insert(AutoJournalLogEntity entity);
}
