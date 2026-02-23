package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.query.GetBalanceSheetQuery;
import com.example.accounting.application.port.in.query.GetBalanceSheetUseCase;
import com.example.accounting.application.port.out.GetBalanceSheetResult;
import com.example.accounting.application.service.BalanceSheetExportService;
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
@RequestMapping("/api/balance-sheet")
@Tag(name = "貸借対照表", description = "貸借対照表に関する API")
public class BalanceSheetController {
    private final GetBalanceSheetUseCase getBalanceSheetUseCase;
    private final BalanceSheetExportService exportService;

    public BalanceSheetController(GetBalanceSheetUseCase getBalanceSheetUseCase,
                                   BalanceSheetExportService exportService) {
        this.getBalanceSheetUseCase = getBalanceSheetUseCase;
        this.exportService = exportService;
    }

    @Operation(summary = "貸借対照表照会", description = "指定した基準日の貸借対照表を照会します")
    @ApiResponse(responseCode = "200", description = "取得成功")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<GetBalanceSheetResult> getBalanceSheet(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalDate comparativeDate
    ) {
        GetBalanceSheetQuery query = new GetBalanceSheetQuery(date, comparativeDate);
        GetBalanceSheetResult result = getBalanceSheetUseCase.execute(query);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "貸借対照表エクスポート", description = "貸借対照表を PDF または Excel 形式でエクスポートします")
    @ApiResponse(responseCode = "200", description = "エクスポート成功")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<byte[]> exportBalanceSheet(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "excel") String format
    ) {
        GetBalanceSheetQuery query = new GetBalanceSheetQuery(date, null);
        GetBalanceSheetResult result = getBalanceSheetUseCase.execute(query);

        if ("pdf".equalsIgnoreCase(format)) {
            return exportService.exportToPdf(result)
                    .map(bytes -> ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=balance-sheet.pdf")
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(bytes))
                    .getOrElseGet(error -> ResponseEntity.internalServerError().build());
        }

        if ("csv".equalsIgnoreCase(format)) {
            return exportService.exportToCsv(result)
                    .map(bytes -> ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=balance-sheet.csv")
                            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                            .body(bytes))
                    .getOrElseGet(error -> ResponseEntity.internalServerError().build());
        }

        return exportService.exportToExcel(result)
                .map(bytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=balance-sheet.xlsx")
                        .contentType(MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(bytes))
                .getOrElseGet(error -> ResponseEntity.internalServerError().build());
    }
}
