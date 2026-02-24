package com.example.accounting.infrastructure.web.dto.audit;

import com.example.accounting.domain.model.audit.AuditAction;
import com.example.accounting.domain.model.audit.AuditLog;
import com.example.accounting.domain.model.audit.EntityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuditLogResponse")
class AuditLogResponseTest {
    private static final String CLIENT_HOST = "client-host";

    @Test
    @DisplayName("entityType ありの監査ログを DTO に変換できる")
    void shouldConvertDomainWithEntityType() {
        AuditLog auditLog = AuditLog.reconstruct(
                1L,
                "user-1",
                AuditAction.CREATE,
                EntityType.JOURNAL_ENTRY,
                "100",
                "仕訳を作成",
                CLIENT_HOST,
                LocalDateTime.of(2026, 2, 1, 10, 30, 45)
        );

        AuditLogResponse response = AuditLogResponse.fromDomain(auditLog);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.userId()).isEqualTo("user-1");
        assertThat(response.actionType()).isEqualTo("CREATE");
        assertThat(response.actionTypeDisplayName()).isEqualTo("作成");
        assertThat(response.entityType()).isEqualTo("JOURNAL_ENTRY");
        assertThat(response.entityTypeDisplayName()).isEqualTo("仕訳");
        assertThat(response.entityId()).isEqualTo("100");
        assertThat(response.description()).isEqualTo("仕訳を作成");
        assertThat(response.ipAddress()).isEqualTo(CLIENT_HOST);
        assertThat(response.createdAt()).isEqualTo("2026-02-01T10:30:45");
    }

    @Test
    @DisplayName("entityType が null の監査ログを DTO に変換できる")
    void shouldConvertDomainWithNullEntityType() {
        AuditLog auditLog = AuditLog.reconstruct(
                2L,
                "user-2",
                AuditAction.LOGIN,
                null,
                null,
                "ログイン成功",
                CLIENT_HOST,
                LocalDateTime.of(2026, 2, 2, 8, 15, 0)
        );

        AuditLogResponse response = AuditLogResponse.fromDomain(auditLog);

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.userId()).isEqualTo("user-2");
        assertThat(response.actionType()).isEqualTo("LOGIN");
        assertThat(response.actionTypeDisplayName()).isEqualTo("ログイン");
        assertThat(response.entityType()).isNull();
        assertThat(response.entityTypeDisplayName()).isNull();
        assertThat(response.createdAt()).isEqualTo("2026-02-02T08:15:00");
    }
}
