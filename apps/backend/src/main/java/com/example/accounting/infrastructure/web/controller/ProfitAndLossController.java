package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.query.GetProfitAndLossQuery;
import com.example.accounting.application.port.in.query.GetProfitAndLossUseCase;
import com.example.accounting.application.port.out.GetProfitAndLossResult;
import com.example.accounting.application.service.ProfitAndLossExportService;
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
@RequestMapping("/api/profit-and-loss")
@Tag(name = "損益計算書", description = "損益計算書に関する API")
public class ProfitAndLossController {
    private final GetProfitAndLossUseCase getProfitAndLossUseCase;
    private final ProfitAndLossExportService exportService;

    public ProfitAndLossController(GetProfitAndLossUseCase getProfitAndLossUseCase,
                                    ProfitAndLossExportService exportService) {
        this.getProfitAndLossUseCase = getProfitAndLossUseCase;
        this.exportService = exportService;
    }

    @Operation(summary = "損益計算書照会", description = "指定した期間の損益計算書を照会します")
    @ApiResponse(responseCode = "200", description = "取得成功")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<GetProfitAndLossResult> getProfitAndLoss(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) LocalDate comparativeDateFrom,
            @RequestParam(required = false) LocalDate comparativeDateTo
    ) {
        GetProfitAndLossQuery query = new GetProfitAndLossQuery(
                dateFrom, dateTo, comparativeDateFrom, comparativeDateTo);
        GetProfitAndLossResult result = getProfitAndLossUseCase.execute(query);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "損益計算書エクスポート", description = "損益計算書を PDF または Excel 形式でエクスポートします")
    @ApiResponse(responseCode = "200", description = "エクスポート成功")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<byte[]> exportProfitAndLoss(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(defaultValue = "excel") String format
    ) {
        GetProfitAndLossQuery query = new GetProfitAndLossQuery(dateFrom, dateTo, null, null);
        GetProfitAndLossResult result = getProfitAndLossUseCase.execute(query);

        if ("pdf".equalsIgnoreCase(format)) {
            return exportService.exportToPdf(result)
                    .map(bytes -> ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=profit-and-loss.pdf")
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(bytes))
                    .getOrElseGet(error -> ResponseEntity.internalServerError().build());
        }

        if ("csv".equalsIgnoreCase(format)) {
            return exportService.exportToCsv(result)
                    .map(bytes -> ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=profit-and-loss.csv")
                            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                            .body(bytes))
                    .getOrElseGet(error -> ResponseEntity.internalServerError().build());
        }

        return exportService.exportToExcel(result)
                .map(bytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=profit-and-loss.xlsx")
                        .contentType(MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(bytes))
                .getOrElseGet(error -> ResponseEntity.internalServerError().build());
    }
}
