package com.example.accounting.application.service;

import com.example.accounting.application.port.in.GetAuditLogsUseCase;
import com.example.accounting.application.port.out.AuditLogRepository;
import com.example.accounting.domain.model.audit.AuditAction;
import com.example.accounting.domain.model.audit.AuditLog;
import com.example.accounting.domain.model.audit.EntityType;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
@ExtendWith(MockitoExtension.class)
class GetAuditLogsServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private GetAuditLogsService service;

    @BeforeEach
    void setUp() {
        service = new GetAuditLogsService(auditLogRepository);
    }

    @Test
    void returnsAuditLogsWithPaginationWhenSearchAndCountSucceed() {
        GetAuditLogsUseCase.GetAuditLogsQuery query = new GetAuditLogsUseCase.GetAuditLogsQuery(
                "user-1",
                AuditAction.CREATE,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31),
                1,
                2
        );

        List<AuditLog> logs = List.of(
                AuditLog.reconstruct(
                        1L,
                        "user-1",
                        AuditAction.CREATE,
                        EntityType.JOURNAL_ENTRY,
                        "100",
                        "created",
                        "127.0.0.1",
                        LocalDateTime.of(2024, 1, 10, 10, 0)
                ),
                AuditLog.reconstruct(
                        2L,
                        "user-1",
                        AuditAction.CREATE,
                        EntityType.ACCOUNT,
                        "200",
                        "created",
                        "127.0.0.1",
                        LocalDateTime.of(2024, 1, 11, 10, 0)
                )
        );

        when(auditLogRepository.search("user-1", AuditAction.CREATE,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), 2, 2))
                .thenReturn(Try.success(logs));
        when(auditLogRepository.countByConditions("user-1", AuditAction.CREATE,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31)))
                .thenReturn(Try.success(5L));

        var result = service.execute(query);

        assertThat(result.isRight()).isTrue();
        var value = result.get();
        assertThat(value.auditLogs()).isEqualTo(logs);
        assertThat(value.totalCount()).isEqualTo(5L);
        assertThat(value.totalPages()).isEqualTo(3);
        assertThat(value.currentPage()).isEqualTo(1);
    }

    @Test
    void returnsLeftWhenSearchFails() {
        GetAuditLogsUseCase.GetAuditLogsQuery query = new GetAuditLogsUseCase.GetAuditLogsQuery(
                "user-1",
                AuditAction.UPDATE,
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 29),
                0,
                20
        );

        when(auditLogRepository.search("user-1", AuditAction.UPDATE,
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29), 0, 20))
                .thenReturn(Try.failure(new RuntimeException("db search error")));

        var result = service.execute(query);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).contains("監査ログの取得に失敗しました");
        assertThat(result.getLeft()).contains("db search error");
    }

    @Test
    void returnsLeftWhenCountFails() {
        GetAuditLogsUseCase.GetAuditLogsQuery query = new GetAuditLogsUseCase.GetAuditLogsQuery(
                "user-1",
                AuditAction.DELETE,
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 31),
                0,
                20
        );

        when(auditLogRepository.search("user-1", AuditAction.DELETE,
                LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31), 0, 20))
                .thenReturn(Try.success(List.of()));
        when(auditLogRepository.countByConditions("user-1", AuditAction.DELETE,
                LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31)))
                .thenReturn(Try.failure(new RuntimeException("db count error")));

        var result = service.execute(query);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).contains("監査ログの件数取得に失敗しました");
        assertThat(result.getLeft()).contains("db count error");
    }

    @Test
    void calculatesTotalPagesCorrectlyForPagination() {
        GetAuditLogsUseCase.GetAuditLogsQuery query = new GetAuditLogsUseCase.GetAuditLogsQuery(
                null,
                null,
                null,
                null,
                0,
                20
        );

        when(auditLogRepository.search(null, null, null, null, 0, 20))
                .thenReturn(Try.success(List.of()));
        when(auditLogRepository.countByConditions(null, null, null, null))
                .thenReturn(Try.success(21L));

        var result = service.execute(query);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get().totalPages()).isEqualTo(2);
    }
}
