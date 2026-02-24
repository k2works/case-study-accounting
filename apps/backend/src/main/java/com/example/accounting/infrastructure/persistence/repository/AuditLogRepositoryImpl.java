package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.application.port.out.AuditLogRepository;
import com.example.accounting.domain.model.audit.AuditAction;
import com.example.accounting.domain.model.audit.AuditLog;
import com.example.accounting.infrastructure.persistence.entity.AuditLogEntity;
import com.example.accounting.infrastructure.persistence.mapper.AuditLogMapper;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {
    private final AuditLogMapper auditLogMapper;

    @Override
    public Try<AuditLog> save(AuditLog auditLog) {
        return Try.of(() -> {
            AuditLogEntity entity = AuditLogEntity.fromDomain(auditLog);
            auditLogMapper.insert(entity);
            return entity.toDomain();
        });
    }

    @Override
    public Try<List<AuditLog>> search(
            String userId,
            AuditAction actionType,
            LocalDate dateFrom,
            LocalDate dateTo,
            int offset,
            int limit
    ) {
        return Try.of(() -> {
            String actionTypeStr = actionType != null ? actionType.name() : null;
            List<AuditLogEntity> entities = auditLogMapper.search(userId, actionTypeStr, dateFrom, dateTo, offset, limit);
            return entities.stream().map(AuditLogEntity::toDomain).toList();
        });
    }

    @Override
    public Try<Long> countByConditions(String userId, AuditAction actionType, LocalDate dateFrom, LocalDate dateTo) {
        return Try.of(() -> {
            String actionTypeStr = actionType != null ? actionType.name() : null;
            return auditLogMapper.countByConditions(userId, actionTypeStr, dateFrom, dateTo);
        });
    }
}
