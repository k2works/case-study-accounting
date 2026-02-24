package com.example.accounting.infrastructure.persistence.mapper;

import com.example.accounting.infrastructure.persistence.entity.AuditLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AuditLogMapper {
    void insert(AuditLogEntity entity);

    List<AuditLogEntity> search(
            @Param("userId") String userId,
            @Param("actionType") String actionType,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    long countByConditions(
            @Param("userId") String userId,
            @Param("actionType") String actionType,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );
}
