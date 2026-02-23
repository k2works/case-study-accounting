package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.GetGeneralLedgerUseCase;
import com.example.accounting.application.port.in.query.GetGeneralLedgerQuery;
import com.example.accounting.application.port.out.GetGeneralLedgerResult;
import com.example.accounting.application.service.GeneralLedgerExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 総勘定元帳照会コントローラ
 */
@RestController
@RequestMapping("/api/general-ledger")
@Tag(name = "総勘定元帳", description = "総勘定元帳に関する API")
public class GeneralLedgerController {

    private final GetGeneralLedgerUseCase getGeneralLedgerUseCase;
    private final GeneralLedgerExportService exportService;

    public GeneralLedgerController(GetGeneralLedgerUseCase getGeneralLedgerUseCase,
                                    GeneralLedgerExportService exportService) {
        this.getGeneralLedgerUseCase = getGeneralLedgerUseCase;
        this.exportService = exportService;
    }

    /**
     * 総勘定元帳照会（ページネーション対応）
     */
    @Operation(
            summary = "総勘定元帳照会",
            description = "経理担当者以上が総勘定元帳を照会します（ページネーション対応）"
    )
    @ApiResponse(
            responseCode = "200",
            description = "取得成功"
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<GetGeneralLedgerResult> getGeneralLedger(
            @RequestParam Integer accountId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        GetGeneralLedgerQuery query = new GetGeneralLedgerQuery(
                accountId,
                dateFrom,
                dateTo,
                page,
                size
        );
        GetGeneralLedgerResult result = getGeneralLedgerUseCase.execute(query);
        return ResponseEntity.ok(result);
    }

    /**
     * 総勘定元帳エクスポート
     */
    @Operation(summary = "総勘定元帳エクスポート", description = "総勘定元帳を CSV または Excel 形式でエクスポートします")
    @ApiResponse(responseCode = "200", description = "エクスポート成功")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<byte[]> exportGeneralLedger(
            @RequestParam Integer accountId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(defaultValue = "excel") String format
    ) {
        GetGeneralLedgerQuery query = new GetGeneralLedgerQuery(accountId, dateFrom, dateTo, 0, 10000);
        GetGeneralLedgerResult result = getGeneralLedgerUseCase.execute(query);

        if ("csv".equalsIgnoreCase(format)) {
            return exportService.exportToCsv(result)
                    .map(bytes -> ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=general-ledger.csv")
                            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                            .body(bytes))
                    .getOrElseGet(error -> ResponseEntity.internalServerError().build());
        }

        return exportService.exportToExcel(result)
                .map(bytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=general-ledger.xlsx")
                        .contentType(MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(bytes))
                .getOrElseGet(error -> ResponseEntity.internalServerError().build());
    }
}
