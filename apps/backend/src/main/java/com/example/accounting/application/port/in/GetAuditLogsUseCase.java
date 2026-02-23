package com.example.accounting.application.port.in;

import com.example.accounting.domain.model.audit.AuditAction;
import com.example.accounting.domain.model.audit.AuditLog;
import io.vavr.control.Either;

import java.time.LocalDate;
import java.util.List;

public interface GetAuditLogsUseCase {

    Either<String, GetAuditLogsResult> execute(GetAuditLogsQuery query);

    record GetAuditLogsQuery(
            String userId,
            AuditAction actionType,
            LocalDate dateFrom,
            LocalDate dateTo,
            int page,
            int size
    ) {
        public GetAuditLogsQuery {
            if (page < 0) {
                page = 0;
            }
            if (size <= 0) {
                size = 20;
            }
        }
    }

    record GetAuditLogsResult(
            List<AuditLog> auditLogs,
            long totalCount,
            int totalPages,
            int currentPage
    ) {
        public GetAuditLogsResult {
            auditLogs = List.copyOf(auditLogs);
        }
    }
}
