package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.audit.AuditAction;
import com.example.accounting.domain.model.audit.AuditLog;
import com.example.accounting.domain.model.audit.EntityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {
    private Long id;
    private String userId;
    private String actionType;
    private String entityType;
    private String entityId;
    private String description;
    private String ipAddress;
    private LocalDateTime createdAt;

    public static AuditLogEntity fromDomain(AuditLog auditLog) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setId(auditLog.getId());
        entity.setUserId(auditLog.getUserId());
        entity.setActionType(auditLog.getActionType().name());
        entity.setEntityType(auditLog.getEntityType() != null ? auditLog.getEntityType().name() : null);
        entity.setEntityId(auditLog.getEntityId());
        entity.setDescription(auditLog.getDescription());
        entity.setIpAddress(auditLog.getIpAddress());
        entity.setCreatedAt(auditLog.getCreatedAt());
        return entity;
    }

    public AuditLog toDomain() {
        return AuditLog.reconstruct(
                id,
                userId,
                AuditAction.valueOf(actionType),
                entityType != null ? EntityType.valueOf(entityType) : null,
                entityId,
                description,
                ipAddress,
                createdAt
        );
    }
}
