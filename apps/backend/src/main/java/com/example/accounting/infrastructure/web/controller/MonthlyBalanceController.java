package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.GetMonthlyBalanceUseCase;
import com.example.accounting.application.port.in.query.GetMonthlyBalanceQuery;
import com.example.accounting.application.port.out.GetMonthlyBalanceResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monthly-balance")
@Tag(name = "月次残高", description = "月次残高に関する API")
public class MonthlyBalanceController {

    private final GetMonthlyBalanceUseCase getMonthlyBalanceUseCase;

    public MonthlyBalanceController(GetMonthlyBalanceUseCase getMonthlyBalanceUseCase) {
        this.getMonthlyBalanceUseCase = getMonthlyBalanceUseCase;
    }

    @Operation(summary = "月次残高照会", description = "指定した勘定科目の月次残高を照会します")
    @ApiResponse(responseCode = "200", description = "取得成功")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<GetMonthlyBalanceResult> getMonthlyBalance(
            @RequestParam String accountCode,
            @RequestParam(required = false) Integer fiscalPeriod
    ) {
        GetMonthlyBalanceQuery query = new GetMonthlyBalanceQuery(accountCode, fiscalPeriod);
        GetMonthlyBalanceResult result = getMonthlyBalanceUseCase.execute(query);
        return ResponseEntity.ok(result);
    }
}
