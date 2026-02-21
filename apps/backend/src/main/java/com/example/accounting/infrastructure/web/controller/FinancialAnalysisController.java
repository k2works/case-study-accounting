package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.query.GetFinancialAnalysisQuery;
import com.example.accounting.application.port.in.query.GetFinancialAnalysisUseCase;
import com.example.accounting.application.port.out.GetFinancialAnalysisResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/financial-analysis")
@Tag(name = "財務分析", description = "財務分析に関する API")
public class FinancialAnalysisController {
    private final GetFinancialAnalysisUseCase getFinancialAnalysisUseCase;

    public FinancialAnalysisController(GetFinancialAnalysisUseCase getFinancialAnalysisUseCase) {
        this.getFinancialAnalysisUseCase = getFinancialAnalysisUseCase;
    }

    @Operation(summary = "財務分析照会", description = "指定した期間の財務分析指標を照会します")
    @ApiResponse(responseCode = "200", description = "取得成功")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<GetFinancialAnalysisResult> getFinancialAnalysis(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) LocalDate comparativeDateFrom,
            @RequestParam(required = false) LocalDate comparativeDateTo
    ) {
        GetFinancialAnalysisQuery query = new GetFinancialAnalysisQuery(
                dateFrom, dateTo, comparativeDateFrom, comparativeDateTo);
        GetFinancialAnalysisResult result = getFinancialAnalysisUseCase.execute(query);
        return ResponseEntity.ok(result);
    }
}
