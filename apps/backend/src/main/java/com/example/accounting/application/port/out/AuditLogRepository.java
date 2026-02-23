package com.example.accounting.application.port.out;

import com.example.accounting.domain.model.audit.AuditAction;
import com.example.accounting.domain.model.audit.AuditLog;
import io.vavr.control.Try;

import java.time.LocalDate;
import java.util.List;

public interface AuditLogRepository {
    Try<AuditLog> save(AuditLog auditLog);

    Try<List<AuditLog>> search(
            String userId,
            AuditAction actionType,
            LocalDate dateFrom,
            LocalDate dateTo,
            int offset,
            int limit
    );

    Try<Long> countByConditions(
            String userId,
            AuditAction actionType,
            LocalDate dateFrom,
            LocalDate dateTo
    );
}
