package com.example.accounting.application.service;

import com.example.accounting.application.port.in.RecordAuditLogUseCase;
import com.example.accounting.application.port.out.AuditLogRepository;
import com.example.accounting.domain.model.audit.AuditAction;
import com.example.accounting.domain.model.audit.AuditLog;
import com.example.accounting.domain.model.audit.EntityType;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
@ExtendWith(MockitoExtension.class)
class RecordAuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private RecordAuditLogService service;

    @BeforeEach
    void setUp() {
        service = new RecordAuditLogService(auditLogRepository);
    }

    @Test
    void returnsRightNullWhenSaveSucceeds() {
        RecordAuditLogUseCase.RecordAuditLogCommand command = new RecordAuditLogUseCase.RecordAuditLogCommand(
                "user-1",
                AuditAction.UPDATE,
                EntityType.JOURNAL_ENTRY,
                "je-1",
                "updated journal entry",
                "127.0.0.1"
        );

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(Try.success(
                AuditLog.reconstruct(
                        1L,
                        "user-1",
                        AuditAction.UPDATE,
                        EntityType.JOURNAL_ENTRY,
                        "je-1",
                        "updated journal entry",
                        "127.0.0.1",
                        LocalDateTime.now()
                )
        ));

        var result = service.execute(command);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isNull();

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("user-1");
        assertThat(captor.getValue().getActionType()).isEqualTo(AuditAction.UPDATE);
        assertThat(captor.getValue().getEntityType()).isEqualTo(EntityType.JOURNAL_ENTRY);
        assertThat(captor.getValue().getEntityId()).isEqualTo("je-1");
        assertThat(captor.getValue().getDescription()).isEqualTo("updated journal entry");
        assertThat(captor.getValue().getIpAddress()).isEqualTo("127.0.0.1");
    }

    @Test
    void returnsLeftWhenSaveFails() {
        RecordAuditLogUseCase.RecordAuditLogCommand command = new RecordAuditLogUseCase.RecordAuditLogCommand(
                "user-1",
                AuditAction.CREATE,
                EntityType.ACCOUNT,
                "ac-1",
                "created account",
                "127.0.0.1"
        );

        when(auditLogRepository.save(any(AuditLog.class)))
                .thenReturn(Try.failure(new RuntimeException("db save error")));

        var result = service.execute(command);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).contains("監査ログの保存に失敗しました");
        assertThat(result.getLeft()).contains("db save error");
    }
}
