package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.query.GetTrialBalanceQuery;
import com.example.accounting.application.port.in.query.GetTrialBalanceUseCase;
import com.example.accounting.application.port.out.GetTrialBalanceResult;
import com.example.accounting.application.service.TrialBalanceExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/trial-balance")
@Tag(name = "残高試算表", description = "残高試算表に関する API")
public class TrialBalanceController {
    private final GetTrialBalanceUseCase getTrialBalanceUseCase;
    private final TrialBalanceExportService exportService;

    public TrialBalanceController(GetTrialBalanceUseCase getTrialBalanceUseCase,
                                   TrialBalanceExportService exportService) {
        this.getTrialBalanceUseCase = getTrialBalanceUseCase;
        this.exportService = exportService;
    }

    @Operation(summary = "残高試算表照会", description = "指定した基準日の残高試算表を照会します")
    @ApiResponse(responseCode = "200", description = "取得成功")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<GetTrialBalanceResult> getTrialBalance(
            @RequestParam(required = false) LocalDate date
    ) {
        GetTrialBalanceQuery query = new GetTrialBalanceQuery(date);
        GetTrialBalanceResult result = getTrialBalanceUseCase.execute(query);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "残高試算表エクスポート", description = "残高試算表を CSV、Excel または PDF 形式でエクスポートします")
    @ApiResponse(responseCode = "200", description = "エクスポート成功")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<byte[]> exportTrialBalance(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "excel") String format
    ) {
        GetTrialBalanceQuery query = new GetTrialBalanceQuery(date);
        GetTrialBalanceResult result = getTrialBalanceUseCase.execute(query);

        if ("pdf".equalsIgnoreCase(format)) {
            return exportService.exportToPdf(result)
                    .map(bytes -> ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=trial-balance.pdf")
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(bytes))
                    .getOrElseGet(error -> ResponseEntity.internalServerError().build());
        }

        if ("csv".equalsIgnoreCase(format)) {
            return exportService.exportToCsv(result)
                    .map(bytes -> ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=trial-balance.csv")
                            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                            .body(bytes))
                    .getOrElseGet(error -> ResponseEntity.internalServerError().build());
        }

        return exportService.exportToExcel(result)
                .map(bytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=trial-balance.xlsx")
                        .contentType(MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(bytes))
                .getOrElseGet(error -> ResponseEntity.internalServerError().build());
    }
}
