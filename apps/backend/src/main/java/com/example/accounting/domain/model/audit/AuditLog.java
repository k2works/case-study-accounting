package com.example.accounting.domain.model.audit;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;

@Value
@With
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditLog {
    Long id;
    String userId;
    AuditAction actionType;
    EntityType entityType;
    String entityId;
    String description;
    String ipAddress;
    LocalDateTime createdAt;

    public static AuditLog create(
            String userId,
            AuditAction actionType,
            EntityType entityType,
            String entityId,
            String description,
            String ipAddress) {
        return AuditLog.builder()
                .userId(userId)
                .actionType(actionType)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .ipAddress(ipAddress)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static AuditLog reconstruct(
            Long id,
            String userId,
            AuditAction actionType,
            EntityType entityType,
            String entityId,
            String description,
            String ipAddress,
            LocalDateTime createdAt) {
        return new AuditLog(id, userId, actionType, entityType, entityId, description, ipAddress, createdAt);
    }
}
