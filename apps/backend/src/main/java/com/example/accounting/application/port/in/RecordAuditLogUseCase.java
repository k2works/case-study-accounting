package com.example.accounting.application.port.in;

import com.example.accounting.domain.model.audit.AuditAction;
import com.example.accounting.domain.model.audit.EntityType;
import io.vavr.control.Either;

public interface RecordAuditLogUseCase {

    Either<String, Void> execute(RecordAuditLogCommand command);

    record RecordAuditLogCommand(
            String userId,
            AuditAction actionType,
            EntityType entityType,
            String entityId,
            String description,
            String ipAddress
    ) {
    }
}
