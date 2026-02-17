package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.GetSubsidiaryLedgerUseCase;
import com.example.accounting.application.port.in.query.GetSubsidiaryLedgerQuery;
import com.example.accounting.application.port.out.GetSubsidiaryLedgerResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 補助元帳照会コントローラ
 */
@RestController
@RequestMapping("/api/subsidiary-ledger")
@Tag(name = "補助元帳", description = "補助元帳に関する API")
public class SubsidiaryLedgerController {

    private final GetSubsidiaryLedgerUseCase getSubsidiaryLedgerUseCase;

    public SubsidiaryLedgerController(GetSubsidiaryLedgerUseCase getSubsidiaryLedgerUseCase) {
        this.getSubsidiaryLedgerUseCase = getSubsidiaryLedgerUseCase;
    }

    /**
     * 補助元帳照会（ページネーション対応）
     */
    @Operation(
            summary = "補助元帳照会",
            description = "経理担当者以上が補助元帳を照会します（ページネーション対応）"
    )
    @ApiResponse(
            responseCode = "200",
            description = "取得成功"
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<GetSubsidiaryLedgerResult> getSubsidiaryLedger(
            @RequestParam String accountCode,
            @RequestParam(required = false) String subAccountCode,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        GetSubsidiaryLedgerQuery query = new GetSubsidiaryLedgerQuery(
                accountCode,
                subAccountCode,
                dateFrom,
                dateTo,
                page,
                size
        );
        GetSubsidiaryLedgerResult result = getSubsidiaryLedgerUseCase.execute(query);
        return ResponseEntity.ok(result);
    }
}
