package com.example.accounting.infrastructure.web.dto.audit;

import com.example.accounting.application.port.in.GetAuditLogsUseCase;

import java.util.List;

public record AuditLogListResponse(
        List<AuditLogResponse> auditLogs,
        long totalCount,
        int totalPages,
        int currentPage
) {
    public static AuditLogListResponse fromResult(GetAuditLogsUseCase.GetAuditLogsResult result) {
        List<AuditLogResponse> auditLogResponses = result.auditLogs().stream()
                .map(AuditLogResponse::fromDomain)
                .toList();

        return new AuditLogListResponse(
                auditLogResponses,
                result.totalCount(),
                result.totalPages(),
                result.currentPage()
        );
    }
}
