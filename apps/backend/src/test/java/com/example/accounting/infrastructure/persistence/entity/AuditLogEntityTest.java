package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.audit.AuditAction;
import com.example.accounting.domain.model.audit.AuditLog;
import com.example.accounting.domain.model.audit.EntityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
@DisplayName("AuditLogEntity 変換")
class AuditLogEntityTest {

    @Test
    @DisplayName("fromDomain でエンティティに変換できる")
    void shouldConvertFromDomain() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 2, 20, 10, 30);
        AuditLog auditLog = AuditLog.reconstruct(
                1L,
                "user001",
                AuditAction.CREATE,
                EntityType.JOURNAL_ENTRY,
                "JE-001",
                "仕訳を作成",
                "192.168.0.1",
                createdAt
        );

        AuditLogEntity entity = AuditLogEntity.fromDomain(auditLog);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getUserId()).isEqualTo("user001");
        assertThat(entity.getActionType()).isEqualTo("CREATE");
        assertThat(entity.getEntityType()).isEqualTo("JOURNAL_ENTRY");
        assertThat(entity.getEntityId()).isEqualTo("JE-001");
        assertThat(entity.getDescription()).isEqualTo("仕訳を作成");
        assertThat(entity.getIpAddress()).isEqualTo("192.168.0.1");
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("toDomain でドメインモデルに変換できる")
    void shouldConvertToDomain() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 2, 20, 10, 30);
        AuditLogEntity entity = new AuditLogEntity(
                2L,
                "user002",
                "UPDATE",
                "ACCOUNT",
                "AC-001",
                "勘定科目を更新",
                "10.0.0.1",
                createdAt
        );

        AuditLog auditLog = entity.toDomain();

        assertThat(auditLog.getId()).isEqualTo(2L);
        assertThat(auditLog.getUserId()).isEqualTo("user002");
        assertThat(auditLog.getActionType()).isEqualTo(AuditAction.UPDATE);
        assertThat(auditLog.getEntityType()).isEqualTo(EntityType.ACCOUNT);
        assertThat(auditLog.getEntityId()).isEqualTo("AC-001");
        assertThat(auditLog.getDescription()).isEqualTo("勘定科目を更新");
        assertThat(auditLog.getIpAddress()).isEqualTo("10.0.0.1");
        assertThat(auditLog.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("entityType が null の場合でも変換できる")
    void shouldHandleNullEntityType() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 2, 20, 10, 30);
        AuditLog auditLog = AuditLog.reconstruct(
                3L,
                "user003",
                AuditAction.LOGIN,
                null,
                null,
                "ログイン",
                "127.0.0.1",
                createdAt
        );

        AuditLogEntity entity = AuditLogEntity.fromDomain(auditLog);

        assertThat(entity.getEntityType()).isNull();

        AuditLog converted = entity.toDomain();
        assertThat(converted.getEntityType()).isNull();
    }
}
