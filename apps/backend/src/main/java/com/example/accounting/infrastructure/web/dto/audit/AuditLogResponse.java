package com.example.accounting.infrastructure.web.dto.audit;

import com.example.accounting.domain.model.audit.AuditLog;

import java.time.format.DateTimeFormatter;

public record AuditLogResponse(
        Long id,
        String userId,
        String actionType,
        String actionTypeDisplayName,
        String entityType,
        String entityTypeDisplayName,
        String entityId,
        String description,
        String ipAddress,
        String createdAt
) {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static AuditLogResponse fromDomain(AuditLog auditLog) {
        String entityType = auditLog.getEntityType() != null ? auditLog.getEntityType().name() : null;
        String entityTypeDisplayName = auditLog.getEntityType() != null
                ? auditLog.getEntityType().getDisplayName()
                : null;

        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getUserId(),
                auditLog.getActionType().name(),
                auditLog.getActionType().getDisplayName(),
                entityType,
                entityTypeDisplayName,
                auditLog.getEntityId(),
                auditLog.getDescription(),
                auditLog.getIpAddress(),
                auditLog.getCreatedAt().format(DATE_TIME_FORMATTER)
        );
    }
}
