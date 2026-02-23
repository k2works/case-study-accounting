package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.GetAuditLogsUseCase;
import com.example.accounting.domain.model.audit.AuditAction;
import com.example.accounting.infrastructure.web.dto.audit.AuditLogListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final GetAuditLogsUseCase getAuditLogsUseCase;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditLogListResponse> getAuditLogs(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Optional<AuditAction> parsedActionType = parseActionType(actionType);
        if (actionType != null && !actionType.isBlank() && parsedActionType.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        GetAuditLogsUseCase.GetAuditLogsQuery query = new GetAuditLogsUseCase.GetAuditLogsQuery(
                userId,
                parsedActionType.orElse(null),
                dateFrom,
                dateTo,
                page,
                size
        );
        var result = getAuditLogsUseCase.execute(query);

        return result.fold(
                error -> ResponseEntity.badRequest().build(),
                success -> ResponseEntity.ok(AuditLogListResponse.fromResult(success))
        );
    }

    private Optional<AuditAction> parseActionType(String actionType) {
        if (actionType == null || actionType.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(AuditAction.valueOf(actionType.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
