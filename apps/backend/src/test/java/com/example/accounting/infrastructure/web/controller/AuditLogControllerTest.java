package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.GetAuditLogsUseCase;
import com.example.accounting.domain.model.audit.AuditAction;
import com.example.accounting.domain.model.audit.AuditLog;
import com.example.accounting.domain.model.audit.EntityType;
import com.example.accounting.infrastructure.web.dto.audit.AuditLogListResponse;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("監査ログコントローラ")
class AuditLogControllerTest {
    private static final String CLIENT_HOST = "client-host";

    @Mock
    private GetAuditLogsUseCase getAuditLogsUseCase;

    private AuditLogController auditLogController;

    @BeforeEach
    void setUp() {
        auditLogController = new AuditLogController(getAuditLogsUseCase);
    }

    @Test
    @DisplayName("パラメータなしで監査ログ一覧を取得できる")
    void shouldGetAuditLogsWithoutFilters() {
        GetAuditLogsUseCase.GetAuditLogsResult success = new GetAuditLogsUseCase.GetAuditLogsResult(
                List.of(sampleAuditLog()),
                1L,
                1,
                0
        );
        when(getAuditLogsUseCase.execute(any(GetAuditLogsUseCase.GetAuditLogsQuery.class)))
                .thenReturn(Either.right(success));

        ResponseEntity<AuditLogListResponse> response = auditLogController.getAuditLogs(
                null, null, null, null, 0, 20
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().auditLogs()).hasSize(1);
        assertThat(response.getBody().totalCount()).isEqualTo(1L);
        assertThat(response.getBody().totalPages()).isEqualTo(1);
        assertThat(response.getBody().currentPage()).isEqualTo(0);

        ArgumentCaptor<GetAuditLogsUseCase.GetAuditLogsQuery> captor =
                ArgumentCaptor.forClass(GetAuditLogsUseCase.GetAuditLogsQuery.class);
        verify(getAuditLogsUseCase).execute(captor.capture());
        assertThat(captor.getValue().userId()).isNull();
        assertThat(captor.getValue().actionType()).isNull();
        assertThat(captor.getValue().dateFrom()).isNull();
        assertThat(captor.getValue().dateTo()).isNull();
        assertThat(captor.getValue().page()).isEqualTo(0);
        assertThat(captor.getValue().size()).isEqualTo(20);
    }

    @Test
    @DisplayName("userId 指定で監査ログ一覧を取得できる")
    void shouldGetAuditLogsWithUserIdFilter() {
        when(getAuditLogsUseCase.execute(any(GetAuditLogsUseCase.GetAuditLogsQuery.class)))
                .thenReturn(Either.right(new GetAuditLogsUseCase.GetAuditLogsResult(List.of(), 0L, 0, 0)));

        ResponseEntity<AuditLogListResponse> response = auditLogController.getAuditLogs(
                "user-1", null, null, null, 0, 20
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<GetAuditLogsUseCase.GetAuditLogsQuery> captor =
                ArgumentCaptor.forClass(GetAuditLogsUseCase.GetAuditLogsQuery.class);
        verify(getAuditLogsUseCase).execute(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo("user-1");
    }

    @Test
    @DisplayName("actionType 指定で監査ログ一覧を取得できる")
    void shouldGetAuditLogsWithActionTypeFilter() {
        when(getAuditLogsUseCase.execute(any(GetAuditLogsUseCase.GetAuditLogsQuery.class)))
                .thenReturn(Either.right(new GetAuditLogsUseCase.GetAuditLogsResult(List.of(), 0L, 0, 0)));

        ResponseEntity<AuditLogListResponse> response = auditLogController.getAuditLogs(
                null, "LOGIN", null, null, 0, 20
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<GetAuditLogsUseCase.GetAuditLogsQuery> captor =
                ArgumentCaptor.forClass(GetAuditLogsUseCase.GetAuditLogsQuery.class);
        verify(getAuditLogsUseCase).execute(captor.capture());
        assertThat(captor.getValue().actionType()).isEqualTo(AuditAction.LOGIN);
    }

    @Test
    @DisplayName("日付範囲指定で監査ログ一覧を取得できる")
    void shouldGetAuditLogsWithDateRangeFilter() {
        LocalDate dateFrom = LocalDate.of(2026, 2, 1);
        LocalDate dateTo = LocalDate.of(2026, 2, 10);
        when(getAuditLogsUseCase.execute(any(GetAuditLogsUseCase.GetAuditLogsQuery.class)))
                .thenReturn(Either.right(new GetAuditLogsUseCase.GetAuditLogsResult(List.of(), 0L, 0, 0)));

        ResponseEntity<AuditLogListResponse> response = auditLogController.getAuditLogs(
                null, null, dateFrom, dateTo, 1, 50
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<GetAuditLogsUseCase.GetAuditLogsQuery> captor =
                ArgumentCaptor.forClass(GetAuditLogsUseCase.GetAuditLogsQuery.class);
        verify(getAuditLogsUseCase).execute(captor.capture());
        assertThat(captor.getValue().dateFrom()).isEqualTo(dateFrom);
        assertThat(captor.getValue().dateTo()).isEqualTo(dateTo);
        assertThat(captor.getValue().page()).isEqualTo(1);
        assertThat(captor.getValue().size()).isEqualTo(50);
    }

    @Test
    @DisplayName("UseCase がエラーを返した場合は 400 を返す")
    void shouldReturnBadRequestWhenUseCaseReturnsError() {
        when(getAuditLogsUseCase.execute(any(GetAuditLogsUseCase.GetAuditLogsQuery.class)))
                .thenReturn(Either.left("検索条件が不正です"));

        ResponseEntity<AuditLogListResponse> response = auditLogController.getAuditLogs(
                null, null, null, null, 0, 20
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    private AuditLog sampleAuditLog() {
        return AuditLog.reconstruct(
                1L,
                "user-1",
                AuditAction.CREATE,
                EntityType.JOURNAL_ENTRY,
                "100",
                "仕訳を作成",
                CLIENT_HOST,
                LocalDateTime.of(2026, 2, 1, 10, 30)
        );
    }
}
