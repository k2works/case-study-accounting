package com.example.accounting.domain.model.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
@DisplayName("監査ログ")
class AuditLogTest {

    @Test
    @DisplayName("create で監査ログを正しく生成できる")
    void shouldCreateAuditLog() {
        AuditLog auditLog = AuditLog.create(
                "user-1",
                AuditAction.LOGIN,
                EntityType.USER,
                "target-1",
                "ログインしました",
                "127.0.0.1"
        );

        assertThat(auditLog.getId()).isNull();
        assertThat(auditLog.getUserId()).isEqualTo("user-1");
        assertThat(auditLog.getActionType()).isEqualTo(AuditAction.LOGIN);
        assertThat(auditLog.getEntityType()).isEqualTo(EntityType.USER);
        assertThat(auditLog.getEntityId()).isEqualTo("target-1");
        assertThat(auditLog.getDescription()).isEqualTo("ログインしました");
        assertThat(auditLog.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(auditLog.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("reconstruct で監査ログを復元できる")
    void shouldReconstructAuditLog() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 2, 23, 12, 30);

        AuditLog auditLog = AuditLog.reconstruct(
                10L,
                "user-2",
                AuditAction.APPROVE,
                EntityType.JOURNAL_ENTRY,
                "journal-100",
                "仕訳を承認しました",
                "192.168.0.1",
                createdAt
        );

        assertThat(auditLog.getId()).isEqualTo(10L);
        assertThat(auditLog.getUserId()).isEqualTo("user-2");
        assertThat(auditLog.getActionType()).isEqualTo(AuditAction.APPROVE);
        assertThat(auditLog.getEntityType()).isEqualTo(EntityType.JOURNAL_ENTRY);
        assertThat(auditLog.getEntityId()).isEqualTo("journal-100");
        assertThat(auditLog.getDescription()).isEqualTo("仕訳を承認しました");
        assertThat(auditLog.getIpAddress()).isEqualTo("192.168.0.1");
        assertThat(auditLog.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("AuditAction の displayName を取得できる")
    void shouldReturnAuditActionDisplayNames() {
        assertThat(AuditAction.LOGIN.getDisplayName()).isEqualTo("ログイン");
        assertThat(AuditAction.LOGOUT.getDisplayName()).isEqualTo("ログアウト");
        assertThat(AuditAction.CREATE.getDisplayName()).isEqualTo("作成");
        assertThat(AuditAction.UPDATE.getDisplayName()).isEqualTo("更新");
        assertThat(AuditAction.DELETE.getDisplayName()).isEqualTo("削除");
        assertThat(AuditAction.APPROVE.getDisplayName()).isEqualTo("承認");
        assertThat(AuditAction.REJECT.getDisplayName()).isEqualTo("差戻し");
        assertThat(AuditAction.CONFIRM.getDisplayName()).isEqualTo("確定");
    }

    @Test
    @DisplayName("EntityType の displayName を取得できる")
    void shouldReturnEntityTypeDisplayNames() {
        assertThat(EntityType.JOURNAL_ENTRY.getDisplayName()).isEqualTo("仕訳");
        assertThat(EntityType.ACCOUNT.getDisplayName()).isEqualTo("勘定科目");
        assertThat(EntityType.USER.getDisplayName()).isEqualTo("ユーザー");
        assertThat(EntityType.AUTO_JOURNAL_PATTERN.getDisplayName()).isEqualTo("自動仕訳パターン");
    }
}
